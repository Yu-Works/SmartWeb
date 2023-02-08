package com.IceCreamQAQ.YuWeb.server.shttp

import com.IceCreamQAQ.SmartWeb.websocket.WsAction
import com.IceCreamQAQ.SmartWeb.websocket.WsContext
import com.IceCreamQAQ.YuWeb.InternalWebServer
import com.IceCreamQAQ.YuWeb.server.shttp.websocket.WsHandler
import kotlinx.coroutines.*
import org.smartboot.http.server.HttpBootstrap
import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse
import org.smartboot.http.server.HttpServerHandler
import org.smartboot.http.server.handler.WebSocketRouteHandler
import java.io.InputStreamReader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class SmartHTTPServer : InternalWebServer() {

    class SmartHttpScope(pool: CoroutineDispatcher) : CoroutineScope {

        override val coroutineContext: CoroutineContext = pool

    }

    val threadPool = ThreadPoolExecutor(
        Runtime.getRuntime().availableProcessors() * 2,
        Runtime.getRuntime().availableProcessors() * 2,
        60L,
        TimeUnit.SECONDS,
        LinkedBlockingQueue()
    ).asCoroutineDispatcher()

    override val pool = SmartHttpScope(threadPool)

    val wsHandler = WebSocketRouteHandler()

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
                        val bound = request.contentType.split("boundary=")[1].trim()

                        val input = request.inputStream!!
//                        input.buffered().re
                        println(bound)
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

                pool.launch {
                    onRequest(req, resp)
                    future.complete(this)
                }

            }
        })
        bootstrap.webSocketHandler(wsHandler)

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

    override fun createWsAction(path: String, action: WsAction) {
        wsHandler.route(path, WsHandler(this, action))
    }

}