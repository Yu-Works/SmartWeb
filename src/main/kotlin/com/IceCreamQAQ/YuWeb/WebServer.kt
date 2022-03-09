package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.IceCreamQAQ.Yu.controller.Router
import com.IceCreamQAQ.Yu.toJSONObject
import com.IceCreamQAQ.YuWeb.controller.render.Render
import com.alibaba.fastjson.JSON
import kotlinx.coroutines.runBlocking
import org.smartboot.http.server.HttpBootstrap
import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse
import org.smartboot.http.common.enums.HttpStatus
import org.smartboot.http.server.HttpServerHandle
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import kotlin.collections.HashMap

abstract class WebServer {

    companion object {
        var jsonDecoder: WebActionContext.(String) -> String = { it }
        var jsonEncoder: WebActionContext.(String) -> String = { it }
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

    protected open lateinit var cache: EhcacheHelp<H.Session>
    open fun sessionCache(cache: EhcacheHelp<H.Session>): WebServer = self {
        this.cache = cache
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

    abstract fun start()
    abstract fun stop()

    open suspend fun onRequest(req: H.Request, resp: H.Response) {

    }

}

class WebServerSS : WebServer() {


    private lateinit var bootstrap: HttpBootstrap
    val enableMethod = arrayOf("get", "post", "put", "delete")

    override fun start() {
        bootstrap = HttpBootstrap()
        bootstrap.configuration().bannerEnabled(false)
        bootstrap.configuration().threadNum(Runtime.getRuntime().availableProcessors() * 2)
        bootstrap.pipeline().next(object : HttpServerHandle() {
            override fun doHandle(request: HttpRequest, response: HttpResponse) {

                val method = request.method.toLowerCase()

                val origin = request.getHeader("Origin")

                if (origin != null) {
                    if (!cors) return
                    if (origin !in corsDomain) return

                    response.setHeader("Access-Control-Allow-Origin", origin)
                    response.setHeader("Access-Control-Allow-Headers", "*")
                    response.setHeader("Access-Control-Allow-Method", "GET,POST,OPTIONS,PUT,DELETE")
                    if (method == "options") return
                }

                if (method !in enableMethod) {
//                    response.
                    response.httpStatus = HttpStatus.valueOf(405)
                    return
                }


                val path = request.requestURI!!
                val contentType = request.contentType ?: ""

                val cookiesString = request.getHeader("Cookie")

                val req =
                    request.run {
                        val headers = arrayListOf<H.Header>()
                        for (name in headerNames) for (header in getHeaders(name)) headers.add(H.Header(name, header))

                        H.Request(
                            scheme = scheme,
                            method = method,
                            path = path,
                            url = requestURL,

                            headers = headers.toTypedArray(),
                            userAgent = getHeader("User-Agent") ?: "",
                            contentType = contentType,
                            charset = characterEncoding,
                            accept = getHeader("Accept") ?: "*/*",

                            queryString = queryString ?: "",

                            userAddress = remoteAddress
                        )
                    }

                var session: H.Session? = null
                if (cookiesString != null) {
                    val cookieStrings = cookiesString.split(";")

                    val cookies = HashMap<String, H.Cookie>()
                    for (cookieStr in cookieStrings) {
                        val kvs = cookieStr.split("=")
                        val cookie = H.Cookie()
                        val key = kvs[0].trim()
                        if (key == "YuSid") {
                            val sid = kvs[1].trim()
                            session = cache[sid]
                            continue
                        }
                        cookie.key = key
                        cookie.value = kvs[1].trim()
                        cookies[key] = cookie
                    }
                    req.cookies = cookies
                }

                if (session == null) {
                    session = createSession()
                    response.addHeader("Set-Cookie", session.toCookie().toCookieString())
                }

//                req.para.putAll(request.parameters)
                for ((k, v) in request.parameters)
                    if (v.size == 1) req.para[k.toParaName()] = v[0]
                    else req.para[k.toParaName()] = v[0]

                req.session = session

                val resp = H.SmartHttpResponse(response)
//                resp.outputStream = response.outputStream

                val p = path.substring(1, path.length).split("/")
                val context = WebActionContext(p.toTypedArray(), req, resp)

                if (method != "get") {
                    val f = contentType.split(";")
                    var charset = "UTF-8"
                    for (i in 1 until f.size) {
                        val s = f[i].trim().split("=")
                        if (s.size != 2) continue
                        when (s[0].trim().toLowerCase()) {
                            "charset" -> charset = s[1].trim()
                        }
                    }
                    when (f[0].trim()) {
                        "application/json" -> {
                            val body = jsonDecoder(context, request.readBody(charset))
                            req.body = body.toJSONObject()
                            for ((k, v) in req.body!!) req.para[k.toParaName()] = v
                        }
                        "application/xml" -> {
                            val body = request.readBody(charset)
//                            req.body = body.toJSONObject()
//                            req.para.putAll(req.body!!)
                        }
                    }
                }

                try {
                    context.success = runBlocking { router.invoke(p[0], context) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    response.httpStatus = HttpStatus.valueOf(500)
                    if (isDev) response.write(
                        e.stackTraceToString()
                            .replace("\t", "    ")
                            .replace(" ", "&nbsp;")
                            .replace("\n", "<br>")
                            .toByteArray()
                    )
                    return
                }
                var result = context.result

                if (!context.success) {
                    if (path.startsWith("/asset/"))
                        this::class.java.classLoader
                            .getResource(path.substring(1))
                            ?.let { result = File(it.file) }
                    if (result == null) {
                        response.httpStatus = HttpStatus.valueOf(404)
                        return
                    }
                }

                if (result is Render) (result as Render).doRender(resp)
                else context.buildResult(result)
            }
        })
        bootstrap.setPort(port).start()
    }


    private fun WebActionContext.makeStringHeader(text: String) =
        when {
            text.startsWith("{") || text.startsWith("[") -> jsonEncoder(text) to "application/json"
            text.startsWith("<?xml") -> text to "application/xml"
            text.startsWith("<") -> text to "text/html"
            else -> text to "text/plain"
        }


    private fun WebActionContext.resultByString(result: String, contextType: String? = null) =
        resultByByteArray(result.toByteArray(), contextType)

    private fun WebActionContext.resultByByteArray(result: ByteArray, contextType: String? = null) {
        contextType?.let { response.contentType = it }
        response.contentLength = result.size.toLong()
        response.write(result)
    }

    private fun WebActionContext.resultByInputStream(
        result: InputStream,
        contextType: String? = null,
        length: Long? = null
    ) {
        length?.let { response.contentLength = it }
        contextType?.let { response.contentType = it }
        response.write(result)
    }

    private fun WebActionContext.buildResult(obj: Any?) {
        invoker?.temple?.let {
            if (request.accept[0] == "text/html") {
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
                    response.header["Content-Disposition"] = "filename=\"${obj.name}\""
                    "application/octet-stream"
                }
                resultByInputStream(FileInputStream(obj), contentType, obj.length())
            }
            else -> resultByString(jsonEncoder(this, JSON.toJSONString(obj)), "application/json")
        }
    }

    fun HttpRequest.readBody(charset: String): String {
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

