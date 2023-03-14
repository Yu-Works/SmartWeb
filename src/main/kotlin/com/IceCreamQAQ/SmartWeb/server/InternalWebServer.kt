package com.IceCreamQAQ.SmartWeb.server

import com.IceCreamQAQ.SmartWeb.controller.WebActionContext
import com.IceCreamQAQ.SmartWeb.controller.WebRootRouter
import com.IceCreamQAQ.SmartWeb.controller.render.Render
import com.IceCreamQAQ.SmartWeb.defaultFileContentType
import com.IceCreamQAQ.SmartWeb.http.*
import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.IceCreamQAQ.Yu.util.subStringByLast
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONArray
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*
import kotlin.collections.HashMap

abstract class InternalWebServer(
    config: WebServerConfig
) : WebServer {

    companion object {
        const val sessionCookieName = "SmartWebSID"
    }

    open val enableMethod: Array<String> = arrayOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")

    val port: Int = config.port
    val isDevMode: Boolean = config.isDevMode
    val rootRouter: WebRootRouter = config.rootRouter
    val sessionCache: EhcacheHelp<Session> = config.sessionCache
    val cors: Boolean
    val corsDomain: Array<String>

    val tmpLocation = File(config.upload.tempDir + "/SmartWeb/${UUID.randomUUID()}")
        .apply { if (!exists()) mkdirs() }

    fun String.makeSize(): Int {
        return when (this.last()) {
            'g' -> this.subStringByLast(1).toInt() * 1024 * 1024 * 1024
            'm' -> this.subStringByLast(1).toInt() * 1024 * 1024
            'k' -> this.subStringByLast(1).toInt() * 1024
            else -> this.toInt()
        }
    }

    val maxUploadSize = config.upload.maxSize.makeSize()
    val maxSingleFileSize = config.upload.maxSize.makeSize()
    val writeTmpFileSize = config.upload.writeTmpFileSize.makeSize()
    val maxUploadFileSize = config.upload.maxFile


    abstract val pool: CoroutineScope

    init {
        cors = config.cors != null
        corsDomain =
            if (cors) config.cors!!.split(",").map { it.trim() }.toTypedArray()
            else arrayOf()
    }

    abstract fun start()
    abstract fun stop()

    open fun findSession(id: String?, resp: Response): Session {
        val sid = id ?: UUID.randomUUID().toString().also { resp.addCookie(Cookie(sessionCookieName, it, true)) }
        return sessionCache.getOrPut(sid, Session(sid, HashMap()))
    }

    open suspend fun onRequest(req: Request, resp: Response) {
        val method = req.method

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
            resp.write()
            return
        }

        val methodEnum = HttpMethod.valueOf(method)

        val path = req.path
        val p = path.substring(1, path.length).split("/")
        val context = WebActionContext(methodEnum, p.toTypedArray(), req, resp)

        req.parameters.forEach { (k, v) ->
            context.params[k] =
                if (v.size == 1)
                    v[0].let {
                        if (it.contains(",")) JSONArray(it.split(","))
                        else it
                    }
                else JSONArray(v.toList())
        }

        req.body?.forEach { (k, v) -> context.params[k] = v }

        val routerMatch: Boolean
        try {
            routerMatch = rootRouter.router(context)
        } catch (e: Throwable) {
            context.runtimeError = e
            context.render500()
            return
        }
        context.runtimeError?.let {
            context.render500()
            return
        }

        var result = context.result

        if (!routerMatch) {
            if (path.startsWith("/asset/"))
                this::class.java.classLoader
                    .getResource(path.substring(1))
                    ?.let { result = File(it.file) }
            if (result == null) {
                resp.status = 404
                resp.write()
                return
            }
        }

        context.buildResult(result)
    }

    open fun WebActionContext.render500() {
        runtimeError!!.printStackTrace()
        resp.status = 500
        if (isDevMode) result = runtimeError!!.stackTraceToString()
            .replace("\t", "    ")
            .replace(" ", "&nbsp;")
            .replace("\n", "<br>")
            .toByteArray()
        buildResult(result)
    }

    open fun WebActionContext.buildResult(obj: Any?) {
        if (obj is Render) return obj.doRender(this, this@InternalWebServer)
        invoker?.temple?.let {
            if (req.accept.mediaType[0] == "text/html") {
                resultByString(it.invoke(this), "text/html")
                return
            }
        }

        fun statusCode(code: Int) {
            resp.status = code
        }
        if (obj == null && requestMethod == HttpMethod.POST) return statusCode(201)
        if (obj == null) return statusCode(204)

        when (obj) {
            is String -> makeStringHeader(obj).let { resultByString(it.first, it.second) }
            is Byte -> resultByByteArray(byteArrayOf(obj))
            is ByteArray -> resultByByteArray(obj)
            is InputStream -> resultByInputStream(obj)
            is UploadFile -> {
                val suffix = obj.name.let { it.substring(it.lastIndexOf(".") + 1) }
                val contentType = defaultFileContentType[suffix] ?: run {
                    resp.addHeader("Content-Disposition", "filename=\"${obj.name}\"")
                    "application/octet-stream"
                }
                resultByInputStream(obj.inputStream, contentType, obj.size)
            }
            is File -> {
                val suffix = obj.name.let { it.substring(it.lastIndexOf(".") + 1) }
                val contentType = defaultFileContentType[suffix] ?: run {
                    resp.addHeader("Content-Disposition", "filename=\"${obj.name}\"")
                    "application/octet-stream"
                }
                resultByInputStream(FileInputStream(obj), contentType, obj.length())
            }

            else -> resultByString(JSON.toJSONString(obj), "application/json")
        }
    }

    open fun WebActionContext.makeStringHeader(text: String) =
        when {
            text.startsWith("{") || text.startsWith("[") -> text to "application/json"
            text.startsWith("<?xml") -> text to "application/xml"
            text.startsWith("<") -> text to "text/html"
            else -> text to "text/plain"
        }


    open fun WebActionContext.resultByString(result: String, contextType: String? = null) =
        resultByByteArray(result.toByteArray(), contextType)

    open fun WebActionContext.resultByByteArray(result: ByteArray, contextType: String? = null) {
        contextType?.let { resp.contentType = it }
        resp.contentLength = result.size.toLong()
        resp.write(result)
    }

    open fun WebActionContext.resultByInputStream(
        result: InputStream,
        contextType: String? = null,
        length: Long? = null
    ) {
        length?.let { resp.contentLength = it }
        contextType?.let { resp.contentType = it }
        resp.write(result)
    }


}