package ea.sports.fut

import com.google.gson.Gson
import ea.sports.fut.model.*
import ea.sports.fut.model.firestore.Authentication
import ea.sports.fut.model.firestore.FifaClub
import ea.sports.fut.model.firestore.User
import exceptions.AuthenticationException
import exceptions.ExpiredSessionException
import exceptions.FutException
import mu.KLogging
import net.dongliu.requests.Methods
import net.dongliu.requests.RawResponse
import utils.Constants
import utils.FirebaseUtil
import utils.RequestsUtil
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*


class Core (dataLogin : Login) : CoreI{


    private val gson  = Gson()
    private lateinit var config : Config
    private var login = dataLogin
    val firebase = FirebaseUtil()
    private lateinit var user : User
    private lateinit var pin : Pin
    private lateinit var club : FifaClub
    private var nRequest = 0
    private var time : Long = 0
    private var players : MutableList<Player> = arrayListOf()
    private var headers : MutableMap<String, String> = hashMapOf()

    init {
        loadPlayers()
        loadConfig()
    }

    /**
     * Load configuration's Json and Headers
     */
    private fun loadConfig(){
        val response = RequestsUtil.getSession().get(Constants.URL_CONFIG).send().toJson()
        config = gson.fromJson<Config>(response, Config::class.java)
        logger.info { "Load config EA Sports" }
        this.user = firebase.getUserDocument(login.email)
        RequestsUtil.loadCookies(user)
        loginBasic()
    }

    /**
     * Login without token
     */
    private fun loginBasic(){
        logger.info { "Start login .. FUT 19" }
        var gameSku : String
        when (login.platform){
            Platform.PS4 -> gameSku = "${Constants.PRE_GAME_SKU}${Platform.PS4.sku}"
            Platform.PC -> gameSku = "${Constants.PRE_GAME_SKU}${Platform.PC.sku}"
            Platform.XBOXONE -> gameSku = "${Constants.PRE_GAME_SKU}${Platform.XBOXONE.sku}"
        }
        var params : HashMap<String, Any?> = hashMapOf("redirect_uri" to Constants.URL_WEBAPP_AUTH,
                "prompt" to "login",
                "client_id" to config.eadpClientId,
                "accessToken" to user.auth.token,
                "locale" to "en_US",
                "scope" to "basic.identity offline signin",
                "display" to "web2/login",
                "response_type" to "token",
                "release_type" to "prod")
        headers = hashMapOf("Referer" to Constants.URL_WEBAPP)
        var response = RequestsUtil.get(Constants.URL_EA_ACCOUNTS, params = params, headers = headers)
        logger.info { "Login with email and password" }
        if (response.url != Constants.URL_WEBAPP_AUTH){
            headers["Referer"] = response.url
            params = hashMapOf("email"  to login.email,
                    "password" to login.password,
                    "country" to "US",
                    "phoneNumber" to "",
                    "passwordForPhone" to "",
                    "gCaptchaResponse" to "",
                    "isPhoneNumberLogin" to "false",
                    "isIncompletePhone" to "",
                    "_rememberMe" to "on",
                    "rememberMe" to "on",
                    "_eventId" to "submit")
            response = RequestsUtil.post(response.url, headers = headers, body = params)
            var regex = "general-error\">\\s+<div>\\s+<div>\\s+(.*)\\s.+".toRegex()
            if ("successfulLogin': false" in response.text()){
                regex.matchEntire(response.text())?.destructured?.let {
                    error -> throw AuthenticationException("Error -> $error")
                }
            }

            if ("var redirectUri" in response.text()){
                response = RequestsUtil.get("${response.url}&_eventId=end")
                if ("Login Verification" in response.text()){
                    logger.info { "Login require verification" }
                    when (login.typeCodeConfirmation){
                        TypeCodeConfirmation.SMS -> {
                            response = RequestsUtil.post(response.url, body = hashMapOf("_eventId" to "submit", "codeType" to TypeCodeConfirmation.SMS.type))
                            logger.info { "Sent code to SMS" }
                        }
                        TypeCodeConfirmation.EMAIL -> {
                            response = RequestsUtil.post(response.url, body = hashMapOf("_eventId" to "submit", "codeType" to TypeCodeConfirmation.EMAIL.type))
                            logger.info { "Sent code to EMAIL" }
                        }
                    }
                }
                if ("Enter your security code" in response.text()){
                    print("Enter your code received : ")
                    val input = Scanner(System.`in`)
                    var code = input.nextLine()
                    headers["Referer"] = response.url
                    response = RequestsUtil.post(response.url.replace("s3", "s4"),
                            body = hashMapOf("oneTimeCode" to code,
                                    "_trustThisDevice" to "on",
                                    "trustThisDevice" to "on",
                                    "_eventId" to "submit"))
                    if ("Incorrect code entered" in response.text() || "Please enter a valid security code" in response.text()){
                        logger.error { "Error during login process - provided code is incorrect" }
                        throw AuthenticationException("Error during login process - provided code is incorrect")
                    }
                    logger.info { "Code is correct, continuing with login" }
                }
                regex = "https://www.easports.com/fifa/ultimate-team/web-app/auth.html#access_token=(.+?)&token_type=(.+?)&expires_in=[0-9]+".toRegex()
                val auth = regex.matchEntire(response.url)!!.destructured.let { (access_token, token_type) ->  Authentication(access_token, token_type) }
                user = saveSession(auth)
                response = RequestsUtil.get(Constants.URL_WEBAPP)
                headers = hashMapOf("Referer" to Constants.URL_WEBAPP,
                        "Accept" to Constants.JSON,
                        "Authorization" to "${user.auth.type_token} ${user.auth.token}")
                var jsonObject = RequestsUtil.get("https://gateway.ea.com/proxy/identity/pids/me", headers = headers).toJson()
                val identity = Gson().fromJson(jsonObject["pid"], Identity::class.java)
                headers["Easw-Session-Data-Nucleus-Id"] = identity.externalRefValue
                headers.remove("Authorization")
                time = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)*1000
                logger.info { "time : $time" }
                response = RequestsUtil.get("https://${config.authURL}/ut/shards/v2", params = hashMapOf("_" to time.toString()), headers = headers)
                time ++
                logger.info { "time : $time" }

                params = linkedMapOf(
                        "sku" to Constants.GAME_SKU,
                        "filterConsoleLogin" to "true",
                        "returningUserGameYear" to "2018",
                        "_" to time)

                logger.info { "Getting account" }
                jsonObject = RequestsUtil.get("https://${login.platform.host}/ut/game/fifa19/user/accountinfo", params = params,  headers = headers).toJson()
                time ++
                logger.info { "time : $time" }
                jsonObject = jsonObject["userAccountInfo"].asJsonObject
                val persona = Gson().fromJson(jsonObject["personas"].asJsonArray[0], Persona::class.java) ?: throw AuthenticationException("Persona not found ")
                logger.info { "Persona : ${persona.personaName}" }
                headers.remove("Easw-Session-Data-Nucleus-Id")
                headers["Origin"] = Constants.URL_EASPORTS
                params = hashMapOf("client_id" to "FOS-SERVER",
                        "redirect_uri" to "nucleus:rest",
                        "response_type" to "code",
                        "access_token" to user.auth.token,
                        "release_type" to "prod")

                jsonObject = RequestsUtil.get(Constants.URL_EA_ACCOUNTS, params = params, headers = headers).toJson()
                var authCode = jsonObject["code"]
                headers["Content-Type"] = Constants.JSON

                var data : HashMap<String, Any?>  = hashMapOf("isReadOnly" to "false",
                        "sku" to Constants.GAME_SKU,
                        "clientVersion" to 1,
                        "nucleusPersonaId" to persona.personaId,
                        "gameSku" to gameSku,
                        "locale" to "en-US",
                        "method" to "authcode",
                        "priorityLevel" to 4,
                        "identification" to mapOf("authCode" to authCode.asString,
                                "redirectUrl" to "nucleus:rest"))

                logger.info { "time : $time" }
                params = hashMapOf("sku_b" to Constants.GAME_SKU_2, "" to LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)*1000)

                jsonObject = RequestsUtil.post("https://${login.platform.host}/ut/auth", params = params, body = data, json = true, headers = headers).toJson()
                val sid = jsonObject["sid"].asString

                logger.info { "Validating if is necessary security question " }
                headers["X-UT-SID"] = sid
                headers["Easw-Session-Data-Nucleus-Id"] = identity.externalRefValue
                jsonObject = RequestsUtil.get("https://${login.platform.host}/ut/game/fifa19/phishing/question", params = hashMapOf("_" to time), headers = headers).toJson()
                time ++
                logger.info { "time : $time" }
                if (jsonObject["code"].asString == "458"){
                    throw FutException( "${jsonObject["code"]} : ${jsonObject["reason"]}")
                }
                if (jsonObject["string"].asString != "Already answered question"
                            && jsonObject["string"].asString != "Feature Disabled"){
                    logger.info { "Require secret answer" }
                    params = hashMapOf("answer" to login.secretAnswer)
                    jsonObject = RequestsUtil.post("https://${login.platform.host}/ut/game/fifa19/phishing/validate", body = params, headers = headers).toJson()
                    if (jsonObject["string"].asString == "Phishing feature is disabled"){
                        logger.error { "Phishing feature is disabled" }
                        throw AuthenticationException("Phishing feature is disabled" )
                    }else if (jsonObject["string"].asString == "OK"){
                        logger.error { "Error with answer question -> ${jsonObject["reason"].asString}" }
                        throw AuthenticationException("Error with answer question -> ${jsonObject["reason"].asString}")
                    }
                    var token = jsonObject["token"]
                    headers["X-UT-PHISHING-TOKEN"] = token.asString
                    jsonObject = RequestsUtil.get("https://${login.platform.host}/ut/game/fifa19/phishing/question", params = hashMapOf("_" to time), headers = headers).toJson()
                    time ++
                    logger.info { "time : $time" }
                    token = jsonObject["token"]
                    headers["X-UT-PHISHING-TOKEN"] = token.asString
                }

                pin = Pin(sid = sid, nucleusid = identity.externalRefValue, personaId = persona.personaId, platform = login.platform, dob = identity.dob.substring(0, identity.dob.length-3), pingURl = config.pinURL, releaseType = config.releaseType)
                var events = pin.event("login", status = "success")
                pin.send(listOf(events))

                params = hashMapOf("_" to time)
                jsonObject = RequestsUtil.get("https://${login.platform.host}/ut/game/fifa19/usermassinfo", params = params, headers = headers).toJson()

                if (jsonObject["userInfo"]
                                .asJsonObject["feature"]
                                .asJsonObject["trade"].asInt == 0){
                    logger.info { "Transfer market is probably disabled on this account." }
                    throw FutException("Transfer market is probably disabled on this account.")
                }
                club = FifaClub(personaId = jsonObject["userInfo"].asJsonObject["personaId"].asLong,
                        clubName = jsonObject["userInfo"].asJsonObject["clubName"].asString,
                        personaName = jsonObject["userInfo"].asJsonObject["personaName"].asString,
                        clubAbbr = jsonObject["userInfo"].asJsonObject["clubAbbr"].asString,
                        loss = jsonObject["userInfo"].asJsonObject["loss"].asLong,
                        won = jsonObject["userInfo"].asJsonObject["won"].asLong,
                        draw = jsonObject["userInfo"].asJsonObject["draw"].asLong,
                        credits = jsonObject["userInfo"].asJsonObject["credits"].asLong,
                        divisionOffline = jsonObject["userInfo"].asJsonObject["divisionOffline"].asLong,
                        divisionOnline = jsonObject["userInfo"].asJsonObject["divisionOnline"].asString)
                user.club = club
                user = saveSession()
                pin.send(listOf(pin.event("page_view", "Hub - Home")))
                pin.send(listOf(pin.event("connection"), pin.event("boot_end", endReason = "normal") ))
                tradePile()
                watchList()
            }
        }
    }

    private fun makeRequest(path : String, data : HashMap<String, Any?>? = null, params : HashMap<String, Any?>? = null, fast : Boolean = false, method : String, json : Boolean = true) : RawResponse{
        val url = "https://${login.platform.host}/ut/game/fifa19/$path"
        var localParams : MutableMap<String, Any?> = hashMapOf()
        var localData : MutableMap<String, Any?> = hashMapOf()
        params?.forEach { key, value -> localParams[key] = value}
        data?.forEach { key, value -> localData[key] = value}
        logger.info { "request: $url data=$data;  params=$params" }
        if (method == "GET"){
            localParams["_"] = time
            time++
        }
        if (fast){
            kotlin.run { Thread.sleep(1000) } // Minimum delay between request
        }else{
            kotlin.run { Thread.sleep((10..25).shuffled().last().toLong() * 100) }
        }
        var response : RawResponse = RequestsUtil.getSession().options(url).send()
        try {
            if (method.toUpperCase() == Methods.GET){
                response = RequestsUtil.get(url, params = localParams, headers = headers)
            } else if (method.toUpperCase() == Methods.POST){
                response = RequestsUtil.post(url, params = localParams, body = localData, json = json, headers = headers)
            } else if (method.toUpperCase() == Methods.PUT){
                response = RequestsUtil.put(url, params = localParams, body = localData, json = json, headers = headers)
            } else if (method.toUpperCase() == Methods.DELETE){
                response = RequestsUtil.delete(url, params = localParams, headers = headers)
            }
            logger.info { "Response : ${response.text()}" }
            nRequest++
        }catch (e : ExpiredSessionException){
            logger.info { "Session expired  : $e" }
            throw ExpiredSessionException(e.message)
        }catch (e : FutException){
            logger.info { "Exception  : $e" }
        }

        if (response.text() != ""){
            var jsonObject = response.toJson()
            if ("credits" in response.text()){
                user.club!!.credits = jsonObject["credits"].asLong
                user = saveSession()
            }
        }

        return response
    }

    /**
     * Save user in firestore with information update
     */
    private fun saveSession(auth : Authentication = user.auth) : User{
        val cookies = RequestsUtil.getCurrentCookies()
        user.auth = auth
        user.cookies = cookies
        firebase.updateUser(user)
        return user
    }


    /**
     * Make a bid to an item
     */
    override fun bid(auctionInfo: AuctionInfo, fast: Boolean, bid : Long) : Boolean{
        val url = "trade/${auctionInfo.tradeId}/bid"
        logger.info { "trade : $auctionInfo" }
        if (!fast){
            val trade = tradeStatus(listOf(auctionInfo))[0]

            if ((trade.currentBid >= bid) || (user.club!!.credits < bid)){
                return false
            }
        }
        val data = hashMapOf<String, Any?>("bid" to bid)
        pin.send(listOf(pin.event("page_view", "Item - Detail View")))
        try {
            val response = makeRequest(url, data = data, method = Methods.PUT, fast = fast).toJson()
            val json = response["auctionInfo"].asJsonArray[0]
            val auction = Gson().fromJson(json, AuctionInfo::class.java)

            when ((auction.bidState == "highest") || (auction.tradeState == "closed" && auction.bidState == "buyNow")){
                true -> {
                    firebase.insertPurchase(auction, user)
                    return true
                }
                false -> return false
            }
        }catch (e : Exception){
            logger.error { e.message }
            return false
        }
    }

    override fun tradeStatus(tradeId: List<AuctionInfo>): List<AuctionInfo> {
        val url = "trade/status"
        val tradeIds = StringBuilder()
        tradeId.forEach { auctionInfo ->
            if (!tradeIds.isBlank()) tradeIds.append(",")
            tradeIds.append(auctionInfo.tradeId)
        }

        val params = hashMapOf<String, Any?>("tradeIds" to tradeIds)

        val response = makeRequest(url, params = params, method = Methods.GET)
        val list : MutableList<AuctionInfo> = arrayListOf()
        val json = response.toJson()
        val array = json["auctionInfo"].asJsonArray
        array.forEach { auction ->
            list.add(Gson().fromJson(auction.asJsonObject, AuctionInfo::class.java))
        }
        return list
    }

    override fun getPlayers() : List<Player>{
        return players
    }

    /**
     * Function to search items in FUT Market
     */
    override fun search(type : TypeCard, level : LevelCard? , category: CategoryConsumables?, assetId : Int? , minPrice : Int? ,
               maxPrice : Int? , minBuy : Int? , maxBuy : Int? , league : Int? , club : Int? ,
               position : String? , nationality : Int? , rare : Boolean? , playStyle : String? ,
               start : Int, pageSize : Int) : List<AuctionInfo>{

        if (start == 0){
            pin.send(listOf(pin.event("page_view", pgdi = "Hub - Transfers")))
            pin.send(listOf(pin.event("page_view", pgdi = "Transfer Market Search")))
        }
        val url = "transfermarket"

        var params = hashMapOf<String, Any?>("start" to start, "num" to pageSize, "type" to type.typeCard)

        if (level != null){
            params["lev"] = level.levelCard
        }
        if (category != null){
            params["cat"] = category.category
        }
        if (assetId != null){
            params["maskedDefId"] = assetId
        }
        if (minPrice != null){
            params["micr"] = minPrice
        }
        if (maxPrice != null){
            params["macr"] = maxPrice
        }
        if (minBuy != null){
            params["minb"] = minBuy
        }
        if (maxBuy != null){
            params["maxb"] = maxBuy
        }
        if (league != null){
            params["leag"] = league
        }
        if (club != null){
            params["team"] = club
        }
        if (position != null){
            params["pos"] = position
        }
        if (nationality != null){
            params["nat"] = nationality
        }
        if (rare != null){
            params["rare"] = "SP"
        }
        if (playStyle != null){
            params["playStyle"] = playStyle
        }

        val  response = makeRequest(url, params = params, method = Methods.GET)

        if (start == 0) {
            pin.send(listOf(pin.event("page_view", pgdi = "Transfer Market Results - List View")))
        }
        val list : MutableList<AuctionInfo> = arrayListOf()
        try {
            val json = response.toJson()
            val array = json["auctionInfo"].asJsonArray
            array.forEach { auction ->
                list.add(Gson().fromJson(auction.asJsonObject, AuctionInfo::class.java))
            }

        }catch (e : Exception){
            logger.error { "Response error $e -> ${response.text()}" }
        }
        return list
    }

    /**
     * Load player from json in a list
     */
    private fun loadPlayers(){
        logger.info { "Loading player list" }
        var json = RequestsUtil.getSession().get(Constants.URL_PLAYERS).send().toJson()
        var array = json["Players"].asJsonArray
        array.forEach { player ->
            players.add(Player(
                id =player.asJsonObject["id"].asInt,
                firstName = player.asJsonObject["f"].asString,
                lastName = player.asJsonObject["l"].asString,
                rating = player.asJsonObject["r"].asInt))
        }

        array = json["LegendsPlayers"].asJsonArray
        array.forEach { player ->
            players.add(Player(
                id =player.asJsonObject["id"].asInt,
                firstName = player.asJsonObject["f"].asString,
                lastName = player.asJsonObject["l"].asString,
                rating = player.asJsonObject["r"].asInt))
        }
        //firebase.updatePlayersEA(players)
        logger.info { "List loaded complete" }
    }

    override fun tradePile(): List<AuctionInfo> {
        val url = "tradepile"
        val response = makeRequest(url, method = Methods.GET)
        val list : MutableList<AuctionInfo> = arrayListOf()
        val json = response.toJson()
        val array = json["auctionInfo"].asJsonArray
        array.forEach { auction ->
            list.add(Gson().fromJson(auction.asJsonObject, AuctionInfo::class.java))
        }
        return list
    }

    override fun watchList(): List<AuctionInfo> {
        val url = "watchlist"
        val response = makeRequest(url, method = Methods.GET)
        val list : MutableList<AuctionInfo> = arrayListOf()
        val json = response.toJson()
        val array = json["auctionInfo"].asJsonArray
        array.forEach { auction ->
            list.add(Gson().fromJson(auction.asJsonObject, AuctionInfo::class.java))
        }
        return list
    }

    companion object : KLogging()


}