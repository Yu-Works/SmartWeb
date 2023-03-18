package com.IceCreamQAQ.SmartWeb.server.undertow.http

import com.IceCreamQAQ.SmartWeb.http.*
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import io.undertow.server.HttpServerExchange
import java.io.InputStream
import java.net.InetSocketAddress

class Req(internal val exc: HttpServerExchange) : Request {
    override val scheme: String
        get() = exc.requestScheme
    override val host: String
        get() = exc.hostAndPort
    override val url: String
        get() = exc.requestURL
    override val path: String
        get() = exc.requestPath
    override val method: String = exc.requestMethod.toString()
    override val headers: Array<Header> = ArrayList<Header>().apply {
        exc.requestHeaders.forEach { it.forEach { value -> add(Header(it.headerName.toString(), value)) } }
    }.toTypedArray()
    override val contentType: String = header("Content-Type")?.value?.split(";")?.get(0)?.trim() ?: ""
    override val charset: String =
        header("Content-Type")?.value
            ?.let { contentType ->
                contentType.indexOf("charset")
                    .let { index ->
                        if (index > 0) contentType.substring(index).split("=")[1].trim()
                        else null
                    }
            } ?: "UTF-8"
    override val queryString: String
        get() = exc.queryString

    override lateinit var parameters: Map<String, Array<String>>
        internal set

    //        get() = HashMap()
    override val cookies: Array<Cookie> = exc.requestCookies().map { Cookie(it.name, it.value) }.toTypedArray()

    override var body: JSONObject? = null
    override val bodyArray: JSONArray? = null
    override val uploadFiles: Map<String, java.util.ArrayList<UploadFile>>? = null

    override val inputStream: InputStream? = null

    override val userAgent: String
        get() = header("User-Agent")?.value ?: ""
    override val userAddress: Array<String>
        get() = TODO("Not yet implemented")
    override val clientAddress: InetSocketAddress
        get() = TODO("Not yet implemented")
    override var cors: CORS? = null

    override val accept: Accept = Accept(
        (header("Accept")?.value ?: "*/*").split(",").map { it.trim() }.toTypedArray(),
        "",
        "",
        ""
    )
    override lateinit var session: Session

    override fun getParameterValues(name: String): Array<String>? = parameters[name]
}