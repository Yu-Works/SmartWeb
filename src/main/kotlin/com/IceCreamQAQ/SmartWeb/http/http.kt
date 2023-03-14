package com.IceCreamQAQ.SmartWeb.http

import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import org.apache.commons.fileupload.FileItem
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.StringBuilder
import java.net.InetSocketAddress
import java.util.*

enum class HttpMethod(val method: String) {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    OPTIONS("OPTIONS"),
    HEAD("HEAD")
}

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
    val bodyArray: JSONArray?
    val uploadFiles: Map<String, ArrayList<UploadFile>>?
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
    fun addCookie(key: String, value: String) =
        addCookie(Cookie(key, value))

    fun addHeader(header: Header)
    fun addHeader(key: String, value: String) =
        addHeader(Header(key, value))

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

    operator fun set(key: String, value: Any?) {
        if (value != null) saves[key] = value
        else saves.remove(key)
    }

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

interface UploadFile {
    val name: String
    val size: Long
    val fieldName: String
    val inputStream: InputStream
    val contentType: String

    fun transferTo(file: File)
}

class CommonsFileUploadFile(
    val fileItem: FileItem
):UploadFile{
    override val name: String
        get() = fileItem.name
    override val size: Long
        get() = fileItem.size
    override val fieldName: String
        get() = fileItem.fieldName
    override val inputStream: InputStream
        get() = fileItem.inputStream
    override val contentType: String
        get() = fileItem.contentType

    override fun transferTo(file: File) {
        fileItem.write(file)
    }
}