package utils

import java.text.SimpleDateFormat
import java.util.*

class Constants {

    companion object {
        const val URL_CONFIG : String = "https://www.easports.com/fifa/ultimate-team/web-app/config/config.json"
        const val URL_WEBAPP_AUTH : String = "https://www.easports.com/fifa/ultimate-team/web-app/auth.html"
        const val URL_WEBAPP : String = "https://www.easports.com/fifa/ultimate-team/web-app/"
        const val URL_EA_ACCOUNTS = "https://accounts.ea.com/connect/auth"
        const val URL_EASPORTS = "http://www.easports.com"
        const val URL_PLAYERS = "https://www.easports.com/fifa/ultimate-team/web-app/content/7D49A6B1-760B-4491-B10C-167FBC81D58A/2019/fut/items/web/players.json"
        const val URL_LOCALE = "https://www.easports.com/fifa/ultimate-team/web-app/loc/en_US.json"
        const val PRE_GAME_SKU : String = "FFA19"
        const val GAME_SKU : String = "FUT19WEB"
        const val GAME_SKU_2 : String = "FFT19"
        const val JSON : String = "application/json"
        var TODAY = SimpleDateFormat("dd-MM-yyyy").format(Date())

        val headers : HashMap<String, String> = hashMapOf(
                "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:62.0) Gecko/20100101 Firefox/62.0",
                "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                "Accept-Encoding" to "gzip,deflate, br",
                "Accept-Language" to "en-US,en;q=0.8",
                "DNT" to "1",
                "Connection" to "keep-alive",
                "Host" to "accounts.ea.com",
                "Upgrade-Insecure-Requests" to "1")


        val cookies_file : String  = "cookies.txt"
        val token_file : String = "token.txt"
        val timeout : Int = 15
        val delay : Int = (1..3).shuffled().last()

    }

}