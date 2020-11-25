package com.IceCreamQAQ.YuWeb

import com.alibaba.fastjson.JSONObject
import java.io.OutputStream
import java.lang.StringBuilder
import java.net.InetAddress
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

    class Response {
        var header: Map<String, String>? = null
        private var cookies: ArrayList<Cookie>? = null
        lateinit var contentType: String
        var body: String? = null
        var charset: String = "UTF-8"
        var status: Int = 200
        lateinit var outputStream: OutputStream

        fun addCookie(cookie: Cookie) {
            if (this.cookies == null) this.cookies = ArrayList()
            this.cookies!!.add(cookie)
        }

        fun getCookies(): ArrayList<Cookie>? {
            return cookies
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