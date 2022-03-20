package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.IceCreamQAQ.Yu.controller.Router
import com.IceCreamQAQ.Yu.toJSONObject
import com.IceCreamQAQ.YuWeb.controller.render.Render
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.smartboot.http.common.enums.HttpStatus
import org.smartboot.http.server.*
import java.io.*
import java.lang.Exception
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

abstract class WebServer {

    companion object {
        val enableMethod = arrayOf("get", "post", "put", "delete")

        var jsonDecoder: (H.Request, H.Response, String) -> JSONObject = { _, _, it -> it.toJSONObject() }
        var jsonEncoder: (H.Request, H.Response, String) -> String = { _, _, it -> it }

        var xmlDecoder: (H.Request, H.Response, String) -> JSONObject = { _, _, it -> TODO() }
        var xmlEncoder: (H.Request, H.Response, String) -> String = { _, _, it -> TODO() }
    }


    open fun self(block: () -> Unit): WebServer {
        block()
        return this
    }

    protected open lateinit var name: String
    open fun name(name: String): WebServer = self {
        this.name = name
    }

    protected open var isDev = false
    open fun isDev(dev: Boolean): WebServer = self {
        this.isDev = dev
    }

    protected open var port: Int = -1
    open fun port(port: Int): WebServer = self {
        this.port = port
    }

    protected open lateinit var router: Router
    open fun router(router: Router): WebServer = self {
        this.router = router
    }

    protected open lateinit var sessionCache: EhcacheHelp<H.Session>
    open fun sessionCache(cache: EhcacheHelp<H.Session>): WebServer = self {
        this.sessionCache = cache
    }

    protected open lateinit var createSession: () -> H.Session
    open fun createSession(createSession: () -> H.Session): WebServer = self {
        this.createSession = createSession
    }

    protected open var cors: Boolean = false
    protected open lateinit var corsDomain: Array<String>
    open fun corsStr(corsStr: String?): WebServer = self {
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
        if (obj is Render) return obj.doRender(this, this@WebServer)
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

class WebServerSS : WebServer() {


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
            response.setContentType(this.contentType)

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
                GlobalScope.launch {
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

