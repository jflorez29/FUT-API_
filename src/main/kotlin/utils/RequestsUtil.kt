package utils

import ea.sports.fut.model.firestore.User
import exceptions.ExpiredSessionException
import exceptions.FutException
import mu.KotlinLogging
import net.dongliu.requests.RawResponse
import net.dongliu.requests.Requests
import net.dongliu.requests.Session

class RequestsUtil {

    companion object {

        private val logger = KotlinLogging.logger {}

        private var session : Session = Requests.session()
        private val headers = Constants.headers
        private val timeout = Constants.timeout*1000

        fun loadCookies(user : User){
            logger.info { "Loading cookies from user" }
            var cookies : MutableList<net.dongliu.requests.Cookie> = arrayListOf()
            if (user.cookies != null){
                for (i : ea.sports.fut.model.firestore.Cookie in user.cookies!!){
                    cookies.add(net.dongliu.requests.Cookie(i.domain, i.path, i.name, i.value, i.expiry!!, i.secure!!, i.hostOnly!!))
                }
                session.setCurrentCookies(cookies)
                logger.info { "Cookies loaded ${session.currentCookies()}" }
            }
        }

        fun get(url: String, headers: Map<String, String>? = null, params: MutableMap<String, Any?>? = null) : RawResponse{
            var localParams : MutableMap<String, Any?> = hashMapOf()
            var localHeaders : MutableMap<String, String> = hashMapOf()
            localHeaders.putAll(this.headers)
            headers?.forEach { key, value -> localHeaders[key] = value }
            params?.forEach { key, value -> localParams[key] = value}
            var response = session.get(url).headers(localHeaders).params(localParams)
                    .timeout(this.timeout)
                    .followRedirect(true)
                    .userAgent(localHeaders["User-Agent"])
                    .send()
            if (response.statusCode == 401){
                throw ExpiredSessionException("Expired session")
            }
            if (response.statusCode == 426 || response.statusCode == 429){
                throw FutException("Too many requests ${response.statusCode}")
            }
            if (response.statusCode == 458){
                throw FutException("Expired session")
            }
            if (response.statusCode == 460 || response.statusCode == 461){
                throw FutException("Permission Denied ${response.statusCode}")
            }
            if (response.statusCode == 478){
                throw FutException("No Trade Existing Error")
            }
            if (response.statusCode == 494){
                throw FutException("Market Locked")
            }
            if (response.statusCode == 500){
                throw FutException("Server are down")
            }
            if (response.statusCode == 512 || response.statusCode == 521){
                throw FutException("Temporary ban or just too many requests. ${response.statusCode}")
            }
            return response
        }

        fun delete(url: String, headers: Map<String, String>? = null, params: MutableMap<String, Any?>? = null) : RawResponse{
            var localParams : MutableMap<String, Any?> = hashMapOf()
            var localHeaders : MutableMap<String, String> = hashMapOf()
            localHeaders.putAll(this.headers)
            headers?.forEach { key, value -> localHeaders[key] = value }
            params?.forEach { key, value -> localParams[key] = value}
            var response =  session.delete(url).headers(localHeaders).params(localParams)
                    .timeout(this.timeout)
                    .followRedirect(true)
                    .userAgent(localHeaders["User-Agent"])
                    .send()
            if (response.statusCode == 401){
                throw FutException("Expired session")
            }
            if (response.statusCode == 426 || response.statusCode == 429){
                throw FutException("Too many requests ${response.statusCode}")
            }
            if (response.statusCode == 458){
                throw FutException("Expired session")
            }
            if (response.statusCode == 460 || response.statusCode == 461){
                throw FutException("Permission Denied ${response.statusCode}")
            }
            if (response.statusCode == 478){
                throw FutException("No Trade Existing Error")
            }
            if (response.statusCode == 494){
                throw FutException("Market Locked")
            }
            if (response.statusCode == 500){
                throw FutException("Server are down")
            }
            if (response.statusCode == 512 || response.statusCode == 521){
                throw FutException("Temporary ban or just too many requests. ${response.statusCode}")
            }
            return response
        }

        fun post(url : String, headers : Map<String, String>? = null, body : MutableMap<String, Any?>? = null, params : MutableMap<String, Any?>? = null, json : Boolean = false) : RawResponse{
            var localParams : MutableMap<String, Any?> = hashMapOf()
            var localBody : MutableMap<String, Any?> = hashMapOf()
            var localHeaders : MutableMap<String, String> = hashMapOf()
            localHeaders.putAll(this.headers)
            headers?.forEach { key, value -> localHeaders[key] = value }
            body?.forEach { key, value -> localBody[key] = value}
            params?.forEach { key, value -> localParams[key] = value}
            val response : RawResponse
            when (json){
                true -> response = session.post(url).headers(localHeaders)
                        .jsonBody(localBody)
                        .params(localParams)
                        .timeout(this.timeout)
                        .followRedirect(true)
                        .userAgent(localHeaders["User-Agent"])
                        .send()
                false -> response = session.post(url).headers(localHeaders)
                        .body(localBody)
                        .params(localParams)
                        .timeout(this.timeout)
                        .followRedirect(true)
                        .userAgent(localHeaders["User-Agent"])
                        .send()
            }
            if (response.statusCode == 401){
                throw FutException("Expired session")
            }
            if (response.statusCode == 426 || response.statusCode == 429){
                throw FutException("Too many requests ${response.statusCode}")
            }
            if (response.statusCode == 458){
                throw FutException("Expired session")
            }
            if (response.statusCode == 460 || response.statusCode == 461){
                throw FutException("Permission Denied ${response.statusCode}")
            }
            if (response.statusCode == 478){
                throw FutException("No Trade Existing Error")
            }
            if (response.statusCode == 494){
                throw FutException("Market Locked")
            }
            if (response.statusCode == 500){
                throw FutException("Server are down")
            }
            if (response.statusCode == 512 || response.statusCode == 521){
                throw FutException("Temporary ban or just too many requests. ${response.statusCode}")
            }
            return response
        }

        fun put(url : String, headers : Map<String, String>? = null, body : MutableMap<String, Any?>? = null, params : MutableMap<String, Any?>? = null, json : Boolean = false) : RawResponse{
            var localParams : MutableMap<String, Any?> = hashMapOf()
            var localBody : MutableMap<String, Any?> = hashMapOf()
            var localHeaders : MutableMap<String, String> = hashMapOf()
            localHeaders.putAll(this.headers)
            headers?.forEach { key, value -> localHeaders[key] = value }
            body?.forEach { key, value -> localBody[key] = value}
            params?.forEach { key, value -> localParams[key] = value}
            val response : RawResponse
            when (json){
                true -> response = session.put(url).headers(localHeaders)
                        .jsonBody(localBody)
                        .params(localParams)
                        .timeout(this.timeout)
                        .followRedirect(true)
                        .userAgent(localHeaders["User-Agent"])
                        .send()
                false -> response = session.put(url).headers(localHeaders)
                        .body(localBody)
                        .params(localParams)
                        .timeout(this.timeout)
                        .followRedirect(true)
                        .userAgent(localHeaders["User-Agent"])
                        .send()
            }
            if (response.statusCode == 401){
                throw FutException("Expired session")
            }
            if (response.statusCode == 426 || response.statusCode == 429){
                throw FutException("Too many requests ${response.statusCode}")
            }
            if (response.statusCode == 458){
                throw FutException("Expired session")
            }
            if (response.statusCode == 460 || response.statusCode == 461){
                throw FutException("Permission Denied ${response.statusCode}")
            }
            if (response.statusCode == 478){
                throw FutException("No Trade Existing Error")
            }
            if (response.statusCode == 494){
                throw FutException("Market Locked")
            }
            if (response.statusCode == 500){
                throw FutException("Server are down")
            }
            if (response.statusCode == 512 || response.statusCode == 521){
                throw FutException("Temporary ban or just too many requests. ${response.statusCode}")
            }
            return response
        }

        fun getCurrentCookies(): MutableList<ea.sports.fut.model.firestore.Cookie> {
            val cookies : MutableList<ea.sports.fut.model.firestore.Cookie> = arrayListOf()
            for (i : net.dongliu.requests.Cookie in session.currentCookies()){
                cookies.add(ea.sports.fut.model.firestore.Cookie(i.domain, i.path, i.expiry, i.isSecure, i.isHostOnly, i.name, i.value))
            }
            return cookies
        }

        fun getSession() : Session {
            return session
        }

    }


}