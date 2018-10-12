package ea.sports.fut.model

import exceptions.FutException
import mu.KLogging
import utils.Constants
import utils.RequestsUtil
import java.time.LocalDateTime


class Pin(val ska : String ? = null, val sid : String = "", val nucleusid : String = "0", val personaId : Int , val dob : String? = null,  val platform: Platform, val pingURl : String, val releaseType : String) {


    private var plat : String
    private var taxv : String
    private var tidt : String
    private var sku : String
    private var rel : String
    private var gid : String
    private var et : String
    private var pidt : String
    private var version : String
    private var s = 1
    private var custom : Any
    private var headers = Constants.headers

    init {
        var response = RequestsUtil.getSession().get("https://www.easports.com/fifa/ultimate-team/web-app/js/compiled_1.js").send()
        var regex = "plat:\"(.+?)\"".toRegex()
        plat = "web"
        if (regex.find(response.text()) != null){
            plat = regex.find(response.text())!!.groups[1]!!.value
        }

        regex = "taxv:\"(.+?)\"".toRegex()
        taxv = regex.find(response.text())!!.groups[1]!!.value

        regex = "tidt:\"(.+?)\"".toRegex()
        tidt = regex.find(response.text())!!.groups[1]!!.value

        regex = "enums.SKU.FUT=\"(.+?)\"".toRegex()
        sku = regex.find(response.text())!!.groups[1]!!.value

        rel = releaseType

        regex = "gid:([0-9]+?)".toRegex()
        gid = regex.find(response.text())!!.groups[1]!!.value

        regex = "et:\"(.+?)\"".toRegex()
        et = regex.find(response.text())!!.groups[1]!!.value

        regex = "pidt:\"(.+?)\"".toRegex()
        pidt = regex.find(response.text())!!.groups[1]!!.value

        regex = "APP_VERSION=\"([0-9\\.]+)\"".toRegex()
        version = regex.find(response.text())!!.groups[1]!!.value

        response = RequestsUtil.getSession().get("https://www.easports.com/fifa/ultimate-team/web-app/js/compiled_2.js").send()

        if (taxv == null){
            regex = "PinManager.TAXONOMY_VERSION=([0-9\\.]+)".toRegex()
            taxv = regex.find(response.text())!!.groups[1]!!.value
        }

        if (tidt == null){
            regex = "PinManager.TAXONOMY_VERSION=([0-9\\.]+)".toRegex()
            taxv = regex.find(response.text())!!.groups[1]!!.value
        }

        headers["Origin"] = "https://www.easports.com"
        headers["Referer"] = "https://www.easports.com/fifa/ultimate-team/web-app/"
        headers["x-ea-game-id"] = sku
        headers["x-ea-game-id-type"] = tidt
        headers["x-ea-taxv"] = taxv

        custom = hashMapOf<String, String>("networkAccess" to "W", "service_plat" to platform.platPin)



    }

    fun getTime() : String {
        return "${LocalDateTime.now()}Z"
    }

    fun event(en : String, pgdi : String? = null, status : String? = null, source : String? = null, endReason : String? = null) : MutableMap<String, Any>{
        val data = hashMapOf<String, Any>("core" to hashMapOf<String, Any>("didm" to hashMapOf("uuid" to "0"),
                "en" to en,
                "pid" to personaId,
                "pidm" to hashMapOf("nucleus" to nucleusid),
                "pidt" to pidt,
                "s" to s,
                "ts_event" to getTime())
        )
        if (dob != null){

            var core : MutableMap<String, Any> = data["core"] as MutableMap<String, Any>
            core["dob"] = dob
            data["core"] = core
        }

        if (pgdi != null){
            data["pgid"] = pgdi
        }
        if (source != null){
            data["source"] = source
        }
        if (status != null){
            data["status"] = status
        }
        if (endReason != null){
            data["end_reason"] = endReason
        }

        if (en == "login"){
            data["type"] = "utas"
            data["userid"] = personaId
        }else if (en == "page_view"){
            data["type"] = "menu"
        }else if (en == "error"){
            data["server_type"] = "utas"
            data["errid"] = "server_error"
            data["type"] = "disconnect"
            data["sid"] = sid
        }
        s++
        return data
    }


    fun send(event : List<MutableMap<String, Any>>){
        logger.info { "Sending pin" }
        Thread.sleep(((0.5*Math.random()/50)*1000).toLong())
        var data = hashMapOf("custom" to custom,
                "et" to et,
                "events" to event,
                "gid" to gid,
                "is_sess" to (sid != ""),
                "loc" to "en_US",
                "plat" to plat,
                "rel" to rel,
                "sid" to sid,
                "taxv" to taxv,
                "tid" to sku,
                "tidt" to tidt,
                "ts_post" to getTime(),
                "v" to version
                )
        logger.info { "data : $data" }
        RequestsUtil.getSession().options(pingURl)
        var json = RequestsUtil.getSession().post(pingURl).jsonBody(data).headers(headers).userAgent(headers["User-Agent"]).send().toJson()

        if (json["status"].asString != "ok"){
            throw FutException("pinEvent is NOT OK, probably they changed something")
        }
        logger.info { "Pin OK" }
    }

    companion object : KLogging()

}