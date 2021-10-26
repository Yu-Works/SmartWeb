package com.IceCreamQAQ.YuWeb

import com.alibaba.fastjson.JSONObject
import org.smartboot.http.server.HttpResponse
import org.smartboot.http.common.enums.HttpStatus
import java.io.OutputStream
import java.lang.StringBuilder
import java.net.InetSocketAddress
import java.util.*
import kotlin.collections.ArrayList

class H {
    class Request(
        val scheme: String,
        val method: String,
        val path: String,
        val url: String,

        val headers: Array<Header>,
        val userAgent: String,
        val contentType: String,
        val charset: String,

        val queryString: String,

        val userAddress: InetSocketAddress,
    ) {
        //        lateinit var header: Map<String, String>
        var cookies: Map<String, Cookie>? = null
        lateinit var session: Session

        var body: JSONObject? = null
        val para = JSONObject()

        fun header(name: String) = headerPrivate(name.toLowerCase())
        private fun headerPrivate(name: String): Header? {
            for (header in headers) {
                if (header.name == name) return header
            }
            return null
        }

        fun headers(name: String) = headersPrivate(name.toLowerCase())
        private fun headersPrivate(name: String) = headers.filter { name == it.name }
    }

    abstract class Response {
        var header: MutableMap<String, String> = hashMapOf()
        private var cookies: ArrayList<Cookie>? = null
        var contentType: String = ""
        var contentLength = -1L
        var body: String? = null
        var charset: String = "UTF-8"
        var status: Int = 200
        abstract val outputStream: OutputStream
        var alive = true

        fun addCookie(cookie: Cookie) {
            if (this.cookies == null) this.cookies = ArrayList()
            this.cookies!!.add(cookie)
        }

        fun getCookies(): ArrayList<Cookie>? {
            return cookies
        }

        open fun makeResp() {

        }
    }

    class SmartHttpResponse(val response: HttpResponse) : Response() {
        override val outputStream: OutputStream
            get() {
                makeResp()
                alive = false
                return response.outputStream
            }

        override fun makeResp() {
            response.characterEncoding = this.charset
            response.setContentType(this.contentType)

            val header = this.header
            for (key in header.keys) {
                response.addHeader(key, header[key])
            }
            val cookies = this.getCookies()
            if (cookies != null) {
                for (cookie in cookies.iterator()) {
                    response.addHeader("Set-Cookie", cookie.toCookieString())
                }
            }
            response.httpStatus = HttpStatus.valueOf(this.status)
            if (this.contentLength > 0) response.setContentLength(this.contentLength.toInt())
        }
    }

    class Cookie {
        lateinit var key: String
        var value: String? = null
        var httpOnly: Boolean = false
        var domain: String? = null
        var path: String? = null
        private var expires: String? = null
        var maxAge: Long? = null

        fun setExpires(date: Date) {
            expires = date.toString()
        }

        fun setExpires(dateStr: String) {
            expires = dateStr
        }

        fun setExpires(date: Long) {
            expires = Date(date).toString()
        }

        fun toCookieString(): String {
            val csb = StringBuilder(key)
            csb.append("=").append(value)

            if (domain != null) csb.append("; Domain=").append(domain)
            if (path != null) csb.append("; Path=").append(path)
            if (expires != null) csb.append("; Expires=").append(expires)
            if (maxAge != null) csb.append("; Max-Age=").append(maxAge.toString())
            if (httpOnly) csb.append("; HttpOnly")

            return csb.toString()
        }
    }

    class Session(var id: String, private var saves: MutableMap<String, Any>) {

        operator fun get(key: String): Any? {
            return saves[key]
        }

        operator fun set(key: String, value: Any) {
            saves[key] = value
        }

        fun toCookie(): Cookie {
            val sc = Cookie();
            sc.key = "YuSid"
            sc.value = id
            sc.httpOnly = true
            return sc
        }
    }

    data class Header(var name: String, val value: String)
}