package com.IceCreamQAQ.YuWeb.server.shttp

import com.IceCreamQAQ.YuWeb.H
import com.IceCreamQAQ.YuWeb.WebServer
import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.*
import org.smartboot.http.common.enums.HttpStatus
import org.smartboot.http.server.HttpBootstrap
import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse
import org.smartboot.http.server.HttpServerHandler
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SmartHTTPServer : WebServer() {

    class SmartHttpScope(pool: CoroutineDispatcher) : CoroutineScope {

        override val coroutineContext: CoroutineContext = pool

    }

    val pool = ThreadPoolExecutor(
        Runtime.getRuntime().availableProcessors() * 2,
        Runtime.getRuntime().availableProcessors() * 2,
        60L,
        TimeUnit.SECONDS,
        LinkedBlockingQueue()
    ).asCoroutineDispatcher()

    val scope = SmartHttpScope(pool)


    private lateinit var bootstrap: HttpBootstrap

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

    class Resp(val response: HttpResponse) : H.Response {

        override var status: Int = 200
        override val cookies = ArrayList<H.Cookie>()
        override val headers = ArrayList<H.Header>()
        override var contentType: String? = null
        override var charset: String? = null
        override var contentLength: Long = -1

        override fun addCookie(cookie: H.Cookie) {
            cookies.add(cookie)
        }

        override fun addHeader(header: H.Header) {
            headers.add(header)
        }

        override fun addHeader(key: String, value: String) {
            headers.add(H.Header(key, value))
        }

        override val output: OutputStream
            get() {
                write()
                return response.outputStream
            }

        override fun write() {
            response.characterEncoding = this.charset
            this.contentType?.let { response.setContentType(it) }

            headers.forEach { response.addHeader(it.name, it.value) }
            cookies.forEach { response.addHeader("Set-Cookie", it.toCookieString()) }

            response.setHttpStatus(HttpStatus.valueOf(this.status))
            if (this.contentLength > 0) response.setContentLength(this.contentLength.toInt())
        }

        override fun write(result: ByteArray) {
            write()
            response.write(result)
        }

        override fun write(result: InputStream) {
            write()
            result.use { it.copyTo(response.outputStream) }
        }

    }

    override fun start() {
        bootstrap = HttpBootstrap()
        bootstrap.configuration().bannerEnabled(false)
        bootstrap.configuration().threadNum(Runtime.getRuntime().availableProcessors() * 2)
        bootstrap.httpHandler(object : HttpServerHandler() {
            override fun handle(request: HttpRequest, response: HttpResponse, future: CompletableFuture<Any>) {
                val req = Req(request)
                val resp = Resp(response)
                if (req.method != "get" && req.method != "head") {

                    if (req.contentType.contains("multipart/form-data")) {
                        val bound = req.contentType.split("boundary=")[1].trim()

                        val input = req.inputStream!!
//                        input.buffered().re
                        println(request.readBody())
                    }

                    req.body = when (req.contentType) {
                        "application/json" -> jsonDecoder(
                            req,
                            resp,
                            request.readBody(request.characterEncoding)
                        )
                        "application/xml" -> xmlDecoder(
                            req,
                            resp,
                            request.readBody(request.characterEncoding)
                        )
                        else -> null
                    }
                }

                req.session = findSession(req.cookie("YuSid")?.value, resp)

//                pool.interceptContinuation()

//                withContext()

                scope.launch {
                    onRequest(req, resp)
                    future.complete(this)
                }

            }
        })
        bootstrap.setPort(port).start()
    }


    fun HttpRequest.readBody(charset: String = "UTF-8"): String {
        val bufferSize = 1024
        val buffer = CharArray(bufferSize)
        val out = StringBuilder()
        val input = InputStreamReader(inputStream, charset)
        while (true) {
            val rsz = input.read(buffer, 0, buffer.size)
            if (rsz < 0)
                break
            out.append(buffer, 0, rsz)
        }
        return out.toString()
    }

    override fun stop() {
        bootstrap.shutdown()
    }

}