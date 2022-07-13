package com.IceCreamQAQ.YuWeb

import com.alibaba.fastjson.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.lang.StringBuilder
import java.net.InetSocketAddress
import java.util.*
import kotlin.collections.ArrayList

//class H {
//    class Request(
//        val scheme: String,
//        val method: String,
//        val path: String,
//        val url: String,
//
//        val headers: Array<Header>,
//        val userAgent: String,
//        val contentType: String,
//        val charset: String,
//        accept: String,
//
//        val queryString: String,
//
//        val userAddress: InetSocketAddress,
//    ) {
//        //        lateinit var header: Map<String, String>
//        val accept: Array<String> = accept.split(";")[0].split(",").map { it.trim().toLowerCase() }.toTypedArray()
//
//        var cookies: Map<String, Cookie>? = null
//        lateinit var session: Session
//
//        var body: JSONObject? = null
//        val para = JSONObject()
//
//        fun header(name: String) = headerPrivate(name.toLowerCase())
//        private fun headerPrivate(name: String): Header? {
//            for (header in headers) {
//                if (header.name == name) return header
//            }
//            return null
//        }
//
//        fun headers(name: String) = headersPrivate(name.toLowerCase())
//        private fun headersPrivate(name: String) = headers.filter { name == it.name }
//    }
//
//    abstract class Response {
//        var header: MutableMap<String, String> = hashMapOf()
//        private var cookies: ArrayList<Cookie>? = null
//        var contentType: String = ""
//        var contentLength = -1L
//        var body: String? = null
//        var charset: String = "UTF-8"
//        var status: Int = 200
//        abstract val outputStream: OutputStream
//        var alive = true
//
//        fun addCookie(cookie: Cookie) {
//            if (this.cookies == null) this.cookies = ArrayList()
//            this.cookies!!.add(cookie)
//        }
//
//        fun getCookies(): ArrayList<Cookie>? {
//            return cookies
//        }
//
//        open fun makeResp() {
//
//        }
//
//        abstract fun write(ba: ByteArray)
//        abstract fun write(input: InputStream)
//    }
//
//    class SmartHttpResponse(val response: HttpResponse) : Response() {
//        override val outputStream: OutputStream
//            get() {
//                makeResp()
//                alive = false
//                return response.outputStream
//            }
//
//        override fun makeResp() {
//            response.characterEncoding = this.charset
//            response.setContentType(this.contentType)
//
//            val header = this.header
//            for (key in header.keys) {
//                response.addHeader(key, header[key])
//            }
//            val cookies = this.getCookies()
//            if (cookies != null) {
//                for (cookie in cookies.iterator()) {
//                    response.addHeader("Set-Cookie", cookie.toCookieString())
//                }
//            }
//            response.httpStatus = HttpStatus.valueOf(this.status)
//            if (this.contentLength > 0) response.setContentLength(this.contentLength.toInt())
//        }
//
//        override fun write(ba: ByteArray) {
//            makeResp()
//            response.write(ba)
//        }
//
//        override fun write(input: InputStream) {
//            makeResp()
//            input.use { it.copyTo(response.outputStream) }
//        }
//    }
//
//    class Cookie {
//        lateinit var key: String
//        var value: String? = null
//        var httpOnly: Boolean = false
//        var domain: String? = null
//        var path: String? = null
//        private var expires: String? = null
//        var maxAge: Long? = null
//
//        fun setExpires(date: Date) {
//            expires = date.toString()
//        }
//
//        fun setExpires(dateStr: String) {
//            expires = dateStr
//        }
//
//        fun setExpires(date: Long) {
//            expires = Date(date).toString()
//        }
//
//        fun toCookieString(): String {
//            val csb = StringBuilder(key)
//            csb.append("=").append(value)
//
//            if (domain != null) csb.append("; Domain=").append(domain)
//            if (path != null) csb.append("; Path=").append(path)
//            if (expires != null) csb.append("; Expires=").append(expires)
//            if (maxAge != null) csb.append("; Max-Age=").append(maxAge.toString())
//            if (httpOnly) csb.append("; HttpOnly")
//
//            return csb.toString()
//        }
//    }
//
//    class Session(var id: String, private var saves: MutableMap<String, Any>) {
//
//        operator fun get(key: String): Any? {
//            return saves[key]
//        }
//
//        operator fun set(key: String, value: Any) {
//            saves[key] = value
//        }
//
//        fun toCookie(): Cookie {
//            val sc = Cookie();
//            sc.key = "YuSid"
//            sc.value = id
//            sc.httpOnly = true
//            return sc
//        }
//    }
//
//    data class Header(var name: String, val value: String)
//}

class H {

    interface Request {
        val scheme: String
        val host: String
        val url: String
        val path: String
        val method: String
        val headers: Array<Header>

        val contentType: String
        val charset: String
        val queryString: String

        val accept: Accept

        val session: Session
        val cookies: Array<Cookie>

        val parameters: Map<String, Array<String>>
        val body: JSONObject?
        val inputStream: InputStream?

        val userAgent: String
        val userAddress: Array<String>
        val clientAddress: InetSocketAddress

        val cors: CORS?

        fun header(name: String): Header? {
            for (header in headers) {
                if (header.name == name) return header
            }
            return null
        }

        fun headers(name: String) = headers.filter { it.name == name }
        fun cookie(name: String): Cookie? {
            for (cookie in cookies) {
                if (cookie.key == name) return cookie
            }
            return null
        }

        fun getParameter(name: String): String? = getParameterValues(name)?.get(0)
        fun getParameterValues(name: String): Array<String>?
    }

    interface Response {
        var status: Int
        val cookies: List<Cookie>

        val headers: List<Header>

        var contentType: String?
        var charset: String?
        var contentLength: Long

        fun addCookie(cookie: Cookie)
        fun addHeader(header: Header)
        fun addHeader(key: String, value: String)

        /***
         * 获取当前会话的输出流。
         * 在获取 output 之前，我们会将 response 携带的所有信息组成 HTTP Head 写入输出流。
         */
        val output: OutputStream

        // 将信息写入 HTTP 输出流。
        fun write()
        // 将信息写入 HTTP 输出流。
        fun write(result: ByteArray)
        // 将信息写入 HTTP 输出流。
        fun write(result: InputStream)
    }

    data class Header(val name: String, val value: String)
    class Cookie(
        var key: String,
        var value: String? = null,
        var httpOnly: Boolean = false,
        var domain: String? = null,
        var path: String? = null,
        var maxAge: Long? = null
    ) {
        private var expires: String? = null

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

    class Session(var id: String, var saves: MutableMap<String, Any>) {

        operator fun get(key: String): Any? {
            return saves[key]
        }

        operator fun set(key: String, value: Any) {
            saves[key] = value
        }

        fun toCookie() = Cookie("YuSid", id, true)
    }

    class Accept(
        val mediaType: Array<String>,
        val charset: String,
        val encoding: String,
        val language: String,
    ) {
        val allMediaType = mediaType.contains("*/*")

        fun findAcceptTypeForce(mediaType: String) = this.mediaType.contains(mediaType)
        fun findAcceptType(mediaType: String): Boolean = allMediaType || this.mediaType.contains(mediaType)
    }

    class CORS
}