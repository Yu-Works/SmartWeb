package smartweb.server.undertow

import smartweb.server.undertow.http.Req
import smartweb.server.undertow.http.Resp
import com.alibaba.fastjson2.JSON
import io.undertow.Undertow
import io.undertow.util.URLUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import smartweb.http.websocket.WsAction
import smartweb.server.InternalWebServer
import smartweb.server.WebServerConfig
import java.nio.ByteBuffer
import java.nio.charset.Charset
import kotlin.coroutines.CoroutineContext

class UndertowServer(config: WebServerConfig) : InternalWebServer(config) {
    lateinit var undertow: Undertow

    override val pool: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext =
            newFixedThreadPoolContext(Runtime.getRuntime().availableProcessors() * 2, "SmartWeb-Worker")
    }

    override fun start() {
        undertow = Undertow.builder()
            .addHttpListener(port, "0.0.0.0")
            .setHandler {

                val parameters = HashMap<String, MutableList<String>>()
                it.queryParameters.forEach { (k, v) -> parameters[k] = v.toMutableList() }

                val req = Req(it)
                val resp = Resp(it)

                if (req.method != "get" && req.method != "head") {

                    if (req.contentType.contains("multipart/form-data")) {
                        val bound = req.contentType.split("boundary=")[1].trim()

                        val input = req.inputStream!!
//                        input.buffered().re
//                    println(request.readBody())
                    }

                    if (req.contentType == "application/x-www-form-urlencoded") {
                        val rsb = StringBuilder()
                        req.readBody()
                            .split("&")
                            .forEach { kv ->
                                kv.split("=").let { kvs ->
                                    parameters.getOrPut(
                                        URLUtils.decode(
                                            kvs[0],
                                            req.charset,
                                            true,
                                            true,
                                            rsb
                                        )
                                    ) { ArrayList() }
                                        .add(
                                            URLUtils.decode(
                                                kvs[1],
                                                req.charset,
                                                true,
                                                true,
                                                rsb
                                            )
                                        )
                                }
                            }
                    }


                    req.body = when (req.contentType) {
                        "application/json" -> JSON.parseObject(req.readBody())
                        "application/xml" -> TODO()
                        else -> null
                    }

                }

                val realParameters = HashMap<String, Array<String>>()
                parameters.forEach { (k, v) -> realParameters[k] = v.toTypedArray() }
                req.parameters = realParameters

                req.session = findSession(req.cookie("YuSid")?.value, resp)

                it.dispatch(Runnable {
                    pool.launch {
                        onRequest(req, resp)
                        it.endExchange()
                    }
                })
            }.build()
        undertow.start()
    }

    override fun stop() {
        undertow.stop()
    }

    override fun createWsAction(path: String, action: WsAction) {

    }

    fun Req.readBody(): String {
        val c = exc.requestContentLength
        val a = ByteArray(c.toInt())
        val b = ByteBuffer.wrap(a)
        exc.requestChannel.read(b)
        return String(a, charset.let { kotlin.runCatching { Charset.forName(it) }.getOrNull() } ?: Charsets.UTF_8)
    }
}