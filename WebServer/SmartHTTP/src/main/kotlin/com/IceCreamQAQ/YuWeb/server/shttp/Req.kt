package com.IceCreamQAQ.YuWeb.server.shttp

import com.IceCreamQAQ.YuWeb.H
import com.alibaba.fastjson2.JSONObject
import org.smartboot.http.server.HttpRequest
import java.io.InputStream
import java.net.InetSocketAddress

class Req(private val request: HttpRequest) : H.Request {
    override val scheme: String
        get() = request.scheme
    override val host: String
        get() = request.remoteHost
    override val url: String
        get() = request.requestURL
    override val path: String
        get() = request.requestURI
    override val method: String = request.method.toLowerCase()
    override val headers: Array<H.Header> = ArrayList<H.Header>().apply {
        request.headerNames.forEach { k ->
            request.getHeaders(k).forEach { v -> add(H.Header(k, v)) }
        }
    }.toTypedArray()
    override val contentType: String
        get() = request.contentType
    override val charset: String
        get() = request.characterEncoding
    override val queryString: String
        get() = request.queryString

    override val parameters: Map<String, Array<String>>
        get() = request.parameters
    override val cookies: Array<H.Cookie> = request.cookies.map { H.Cookie(it.name, it.value) }.toTypedArray()

    override var body: JSONObject? = null
    override val inputStream: InputStream? = null

    override val userAgent: String
        get() = request.getHeader("User-Agent")
    override val userAddress: Array<String>
        get() = TODO("Not yet implemented")
    override val clientAddress: InetSocketAddress
        get() = TODO("Not yet implemented")
    override var cors: H.CORS? = null

    override val accept: H.Accept = H.Accept(
        (request.getHeader("Accept") ?: "*/*").split(",").map { it.trim() }.toTypedArray(),
        "",
        "",
        ""
    )
    override lateinit var session: H.Session

    override fun getParameterValues(name: String): Array<String>? = parameters[name]
}
