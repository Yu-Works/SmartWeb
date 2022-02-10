package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.IceCreamQAQ.Yu.controller.Router
import com.IceCreamQAQ.Yu.toJSONObject
import kotlinx.coroutines.runBlocking
import org.smartboot.http.server.HttpBootstrap
import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse
import org.smartboot.http.common.enums.HttpStatus
import org.smartboot.http.server.HttpServerHandle
import java.io.InputStreamReader
import java.lang.Exception
import kotlin.collections.HashMap

class WebServer(
    private val port: Int,
    corsStr: String?,
    private val router: Router,
    val cache: EhcacheHelp<H.Session>,
    val createSession: () -> H.Session
) {

    val cors: Boolean
    val corsDomain: Array<String>

    val isDev = file("pom.xml", "build.gradle", "build.gradle.kts") != null


    init {
        cors = corsStr != null
        corsDomain =
            if (cors) corsStr!!.split(",").map { it.trim() }.toTypedArray()
            else arrayOf()
    }

    companion object {
        var jsonDecoder: WebActionContext.(String) -> String = { it }
        var jsonEncoder: WebActionContext.(String) -> String = { it }
    }

    private lateinit var bootstrap: HttpBootstrap
    val enableMethod = arrayOf("get", "post", "put", "delete")

    fun start() {
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


                val path = request.requestURI
                val contentType = request.contentType ?: ""

                val cookiesString = request.getHeader("Cookie")

                val req =
                    with(request) {
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
//                    response.write("<!DOCTYPE html>\n<html><body><div>".toByteArray())
                    if (isDev) response.write(
                        e.stackTraceToString().replace("\t", "    ").replace(" ", "&nbsp;").replace("\n", "<br>")
                            .toByteArray()
                    )
//                    response.write("</div></body></html>".toByteArray())
                    return
                }
                if (!context.success) {
                    response.httpStatus = HttpStatus.valueOf(404)
                    return
                }

                if (context.render == null && resp.body == null) {
                    response.httpStatus = HttpStatus.valueOf(204)
                    return
                }

                if (context.render != null) {
                    context.render!!.doRender(resp)
                } else {
                    val ba = resp.body?.toByteArray() ?: byteArrayOf()
                    resp.contentLength = ba.size.toLong()
                    resp.makeResp()
                    response.write(ba)
                }

                if (resp.alive) resp.makeResp()
            }
        })
        bootstrap.setPort(port).start()
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

    fun stop() {
        bootstrap.shutdown()
    }

}

