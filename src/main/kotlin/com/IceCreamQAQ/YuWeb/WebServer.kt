package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.IceCreamQAQ.Yu.controller.Router
import com.IceCreamQAQ.Yu.toJSONObject
import kotlinx.coroutines.runBlocking
import org.smartboot.http.HttpBootstrap
import org.smartboot.http.HttpRequest
import org.smartboot.http.HttpResponse
import org.smartboot.http.enums.HttpStatus
import org.smartboot.http.server.Request
import org.smartboot.http.server.handle.HttpHandle
import java.io.InputStreamReader
import java.lang.Exception
import kotlin.collections.HashMap

class WebServer(private val port: Int, private val router: Router, val cache: EhcacheHelp<H.Session>, val createSession: () -> H.Session) {

    private lateinit var bootstrap: HttpBootstrap

    fun start() {
        bootstrap = HttpBootstrap()
        bootstrap.pipeline().next(object : HttpHandle() {
            override fun doHandle(request: HttpRequest, response: HttpResponse) {

                val method = request.method.toLowerCase()
                if (method != "get" && method != "post") {
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
                                    userAgent = getHeader("User-Agent"),
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
                    if (v.size == 1) req.para[k] = v[0]
                    else req.para[k] = v[0]


                if (method == "post") {
                    if ("application/json" == contentType) {
                        val bufferSize = 1024
                        val buffer = CharArray(bufferSize)
                        val out = StringBuilder()
                        val input = InputStreamReader(request.inputStream, request.characterEncoding)
                        while (true) {
                            val rsz = input.read(buffer, 0, buffer.size)
                            if (rsz < 0)
                                break
                            out.append(buffer, 0, rsz)
                        }
                        req.body = out.toString().toJSONObject()
                        req.para.putAll(req.body!!)
                    }
                }

                req.session = session

                val resp = H.Response()
                resp.outputStream = response.outputStream

                val p = path.substring(1, path.length).split("/")
                val context = WebActionContext(p.toTypedArray(), req, resp)


                try {
                    context.success = runBlocking { router.invoke(p[0], context) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    response.httpStatus = HttpStatus.valueOf(500)
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

                response.characterEncoding = resp.charset
                response.setContentType(resp.contentType)
                val header = resp.header
                if (header != null) {
                    for (key in header.keys) {
                        response.addHeader(key, header[key])
                    }
                }
                val cookies = resp.getCookies()
                if (cookies != null) {
                    for (cookie in cookies.iterator()) {
                        response.addHeader("Set-Cookie", cookie.toCookieString())
                    }
                }
                context.render?.doRender(resp) ?: response.write(resp.body?.toByteArray())

            }
        } as HttpHandle)
        bootstrap.setPort(port).start()
    }

    fun stop() {
        bootstrap.shutdown()
    }

}

