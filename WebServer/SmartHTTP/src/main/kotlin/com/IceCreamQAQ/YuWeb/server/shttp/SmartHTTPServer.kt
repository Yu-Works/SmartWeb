package com.IceCreamQAQ.YuWeb.server.shttp

import com.IceCreamQAQ.SmartWeb.http.CommonsFileUploadFile
import com.IceCreamQAQ.SmartWeb.http.UploadFile
import com.IceCreamQAQ.SmartWeb.http.websocket.WsAction
import com.IceCreamQAQ.SmartWeb.server.InternalWebServer
import com.IceCreamQAQ.SmartWeb.server.WebServerConfig
import com.IceCreamQAQ.YuWeb.server.shttp.websocket.WsHandler
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import kotlinx.coroutines.*
import org.apache.commons.fileupload.FileUpload
import org.apache.commons.fileupload.RequestContext
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.smartboot.http.server.HttpBootstrap
import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse
import org.smartboot.http.server.HttpServerHandler
import org.smartboot.http.server.handler.WebSocketRouteHandler
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class SmartHTTPServer(config: WebServerConfig) : InternalWebServer(config) {

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
        bootstrap.configuration().serverName("SmartWeb|SmartHTTP")
        bootstrap.configuration().threadNum(Runtime.getRuntime().availableProcessors() * 2)
        bootstrap.httpHandler(object : HttpServerHandler() {
            override fun handle(request: HttpRequest, response: HttpResponse, future: CompletableFuture<Any>) {
                val req = Req(request)
                val resp = Resp(response)
                if (req.method != "get" && req.method != "head") {

                    if (req.contentType.contains("multipart/form-data")) {

                        val factory = DiskFileItemFactory()
                        factory.repository = tmpLocation
                        factory.sizeThreshold = writeTmpFileSize
//                        factory

                        val uploadMap = HashMap<String, ArrayList<UploadFile>>()
                        val paramMap = HashMap<String, ArrayList<String>>()

                        val uploads = FileUpload(factory).parseRequest(object : RequestContext {
                            override fun getCharacterEncoding(): String = req.charset
                            override fun getContentType(): String = request.contentType
                            override fun getContentLength(): Int = request.contentLength
                            override fun getInputStream(): InputStream = request.inputStream
                        }).forEach {
                            if (it.isFormField) {
                                val list = paramMap.getOrPut(it.fieldName) { ArrayList() }
                                list.add(it.string)
                            } else {
                                val uploadFile = CommonsFileUploadFile(it)
                                val list = uploadMap.getOrPut(it.fieldName) { ArrayList() }
                                list.add(uploadFile)
                            }
                        }

                        if (paramMap.isNotEmpty()) {
                            req.body = JSONObject()
                            paramMap.forEach { (key, v) ->
                                if (v.size == 1) (req.body as JSONObject)[key] = v[0]
                                else (req.body as JSONObject)[key] = v
                            }
                        }

                        if (uploadMap.isNotEmpty()){
                            req.uploadFiles = uploadMap
                        }

                    }

                    when (req.contentType) {
                        "application/json" -> req.body = JSON.parseObject(request.readBody(request.characterEncoding))
                        "application/xml" -> TODO()
                    }
                }

                req.session = findSession(req.cookie(sessionCookieName)?.value, resp)

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