package com.IceCreamQAQ.YuWeb.server.shttp

import com.IceCreamQAQ.YuWeb.AbstractWebServer
import kotlinx.coroutines.*
import org.smartboot.http.server.HttpBootstrap
import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse
import org.smartboot.http.server.HttpServerHandler
import java.io.InputStreamReader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class SmartHTTPServer : AbstractWebServer() {

    class SmartHttpScope(pool: CoroutineDispatcher) : CoroutineScope {

        override val coroutineContext: CoroutineContext = pool

    }

    private val pool = ThreadPoolExecutor(
        Runtime.getRuntime().availableProcessors() * 2,
        Runtime.getRuntime().availableProcessors() * 2,
        60L,
        TimeUnit.SECONDS,
        LinkedBlockingQueue()
    ).asCoroutineDispatcher()

    val scope = SmartHttpScope(pool)

    private lateinit var bootstrap: HttpBootstrap

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