package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.SmartWeb.WebServer
import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.IceCreamQAQ.Yu.controller.Router
import com.IceCreamQAQ.Yu.event.EventBus
import com.IceCreamQAQ.Yu.toJSONObject
import com.IceCreamQAQ.YuWeb.controller.render.Render
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import java.io.*
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

abstract class InternalWebServer : WebServer {

    companion object {
        val enableMethod = arrayOf("get", "post", "put", "delete")

        var jsonDecoder: (H.Request, H.Response, String) -> JSONObject = { _, _, it -> it.toJSONObject() }
        var jsonEncoder: (H.Request, H.Response, String) -> String = { _, _, it -> it }

        var xmlDecoder: (H.Request, H.Response, String) -> JSONObject = { _, _, it -> TODO() }
        var xmlEncoder: (H.Request, H.Response, String) -> String = { _, _, it -> TODO() }
    }

    abstract val pool: CoroutineScope

    open fun self(block: () -> Unit): InternalWebServer {
        block()
        return this
    }

    protected open lateinit var name: String
    open fun name(name: String): InternalWebServer = self {
        this.name = name
    }

    protected open var isDev = false
    open fun isDev(dev: Boolean): InternalWebServer = self {
        this.isDev = dev
    }

    protected open var port: Int = -1
    open fun port(port: Int): InternalWebServer = self {
        this.port = port
    }

    protected open lateinit var router: Router
    open fun router(router: Router): InternalWebServer = self {
        this.router = router
    }

    protected open lateinit var eventBus: EventBus
    open fun eventBus(eventBus: EventBus): InternalWebServer = self {
        this.eventBus = eventBus
    }

    protected open lateinit var sessionCache: EhcacheHelp<H.Session>
    open fun sessionCache(cache: EhcacheHelp<H.Session>): InternalWebServer = self {
        this.sessionCache = cache
    }

    protected open lateinit var createSession: () -> H.Session
    open fun createSession(createSession: () -> H.Session): InternalWebServer = self {
        this.createSession = createSession
    }

    protected open var cors: Boolean = false
    protected open lateinit var corsDomain: Array<String>
    open fun corsStr(corsStr: String?): InternalWebServer = self {
        cors = corsStr != null
        corsDomain =
            if (cors) corsStr!!.split(",").map { it.trim() }.toTypedArray()
            else arrayOf()
    }

    open fun findSession(id: String?, resp: H.Response): H.Session {
        val sid = id ?: UUID.randomUUID().toString().let { resp.addCookie(H.Cookie("YuSid", it, true)) }
        val psId = "${port}_$sid"
        return sessionCache.getOrPut("${port}_$sid", H.Session(psId, HashMap()))
    }

    abstract fun start()
    abstract fun stop()

    open suspend fun onRequest(req: H.Request, resp: H.Response) {

        val method = req.method.toLowerCase()

        val origin = req.header("Origin")?.value

        if (origin != null) {
            if (!cors) return
            if (origin !in corsDomain) return

            resp.addHeader("Access-Control-Allow-Origin", origin)
            resp.addHeader("Access-Control-Allow-Headers", "*")
            resp.addHeader("Access-Control-Allow-Method", "GET,POST,OPTIONS,PUT,DELETE")
            if (method == "options") return
        }

        if (method !in enableMethod) {
            resp.status = 405
            return
        }


        val path = req.path
        val p = path.substring(1, path.length).split("/")
        val context = WebActionContext(p.toTypedArray(), req, resp)
        req.parameters.forEach { (k, v) ->
            context.paras[k] =
                if (v.size == 1)
                    v[0].let {
                        if (it.contains(",")) JSONArray(it.split(","))
                        else it
                    }
                else JSONArray(v.toList())
        }
        req.body?.forEach { (k, v) -> context.paras[k] = v }

        try {
            context.success = runBlocking { router.invoke(p[0], context) }
        } catch (e: Exception) {
            e.printStackTrace()
            resp.status = 500
            if (isDev) context.result = e.stackTraceToString()
                .replace("\t", "    ")
                .replace(" ", "&nbsp;")
                .replace("\n", "<br>")
                .toByteArray()
            return
        }
        var result = context.result

        if (!context.success) {
            if (path.startsWith("/asset/"))
                this::class.java.classLoader
                    .getResource(path.substring(1))
                    ?.let { result = File(it.file) }
            if (result == null) {
                resp.status = 404
                return
            }
        }

        context.buildResult(result)
//        if (result is Render) (result as Render).doRender(resp)
//        else
//
//        val routers = req.method?.findRouter(this)
//        val router = routers?.run {
//            staticRouter[req.path] ?: matchRouter.firstOrNull { r -> r.match(context) }
//        }
//        val code: Int
//        var result: Any? = null
//        if (routers == null) {
//            code = 501
//            result = on501(context)
//        } else if (router == null) {
//            code = 404
//            result = on404(context)
//        } else {
//            context.router = router
//            router(context)
//            if (context.error != null) {
//                code = 500
//                result = on500(context, context.error!!)
//            } else if (context.result != null) {
//                code = 200
//                result = context.result
//            } else {
//                code = if (context.router?.templateInvoker != null && req.accept.findAcceptType("text/html")) {
//                    result = context.router!!.templateInvoker!!(context)
//                    200
//                } else if (req.method == Method.POST) 201 else 204
//            }
//
//        }
//        resp.status = code
//        return result?.let { resultBuilder(context, it) }
    }

    open fun WebActionContext.makeStringHeader(text: String) =
        when {
            text.startsWith("{") || text.startsWith("[") -> jsonEncoder(request, response, text) to "application/json"
            text.startsWith("<?xml") -> text to "application/xml"
            text.startsWith("<") -> text to "text/html"
            else -> text to "text/plain"
        }


    open fun WebActionContext.resultByString(result: String, contextType: String? = null) =
        resultByByteArray(result.toByteArray(), contextType)

    open fun WebActionContext.resultByByteArray(result: ByteArray, contextType: String? = null) {
        contextType?.let { response.contentType = it }
        response.contentLength = result.size.toLong()
        response.write(result)
    }

    open fun WebActionContext.resultByInputStream(
        result: InputStream,
        contextType: String? = null,
        length: Long? = null
    ) {
        length?.let { response.contentLength = it }
        contextType?.let { response.contentType = it }
        response.write(result)
    }

    open fun WebActionContext.buildResult(obj: Any?) {
        if (obj is Render) return obj.doRender(this, this@InternalWebServer)
        invoker?.temple?.let {
            if (request.accept.mediaType[0] == "text/html") {
                resultByString(it.invoke(this), "text/html")
                return
            }
        }

        fun statusCode(code: Int) {
            response.status = code
        }
        if (obj == null && request.method == "post") return statusCode(201)
        if (obj == null) return statusCode(204)

        when (obj) {
            is String -> makeStringHeader(obj).let { resultByString(it.first, it.second) }
            is Byte -> resultByByteArray(byteArrayOf(obj))
            is ByteArray -> resultByByteArray(obj)
            is InputStream -> resultByInputStream(obj)
            is File -> {
                val suffix = obj.name.let { it.substring(it.lastIndexOf(".") + 1) }
                val contentType = defaultFileContentType[suffix] ?: run {
                    response.addHeader("Content-Disposition", "filename=\"${obj.name}\"")
                    "application/octet-stream"
                }
                resultByInputStream(FileInputStream(obj), contentType, obj.length())
            }

            else -> resultByString(jsonEncoder(request, response, JSON.toJSONString(obj)), "application/json")
        }
    }

}