package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.IceCreamQAQ.Yu.controller.router.NewRouter
import com.IceCreamQAQ.Yu.controller.router.RouterPlus
import org.smartboot.http.HttpBootstrap
import org.smartboot.http.HttpRequest
import org.smartboot.http.HttpResponse
import org.smartboot.http.enums.HttpStatus
import org.smartboot.http.server.handle.HttpHandle
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class WebServer(private val port: Int, private val router: NewRouter, val cache: EhcacheHelp<H.Session>,val createSession:() -> H.Session) {

    private lateinit var bootstrap: HttpBootstrap

    fun start() {
        bootstrap = HttpBootstrap()
        bootstrap.pipeline().next(object : HttpHandle() {
            override fun doHandle(request: HttpRequest, response: HttpResponse) {

                val method = request.method
                val path = request.requestURI
                val contentType = request.contentType ?: ""

                val cookiesString = request.getHeader("Cookie")

                val req = H.Request(method, path, contentType)

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
//                {
//                    val sid = UUID.randomUUID().toString()
//                    val psId = "${port}_$sid"
//                    session = H.Session(psId, HashMap())
//                    cache[psId] = session
//                    response.addHeader("Set-Cookie", session.toCookie().toCookieString())
//                }

                if (method == "POST") {
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
                        req.body = out.toString()
                    }
                }
                req.para = request.parameters
                req.session = session

                val resp = H.Response()
                resp.outputStream = response.outputStream

                val p = path.substring(1, path.length).split("/")
                val context = WebActionContext(p.toTypedArray(),req,resp)


                try {
                    context.success = router.invoke(p[0], context)
                } catch (e: Exception) {
                    e.printStackTrace()
                    response.httpStatus = HttpStatus.valueOf(500)
                    return
                }
                if (!context.success) {
                    response.httpStatus = HttpStatus.valueOf(404)
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
                context.render?.doRender(resp)?: response.write(resp.body?.toByteArray())

            }
        } as HttpHandle)
        bootstrap.setPort(port).start()
    }

    fun stop() {
        bootstrap.shutdown()
    }

//    class Handler(private val port: Int, val router: RouterPlus, private val cache: EhcacheHelp<H.Session>) : HttpHandle() {
//        @Throws(IOException::class)
//        override fun doHandle(request: HttpRequest, response: HttpResponse) {
//
//            val method = request.method
//            val path = request.requestURI
//            val contentType = request.contentType ?: ""
//
//            val cookiesString = request.getHeader("Cookie")
//
//            val req = H.Request(method, path, contentType)
//
//            var session: H.Session? = null
//            if (cookiesString != null) {
//                val cookieStrings = cookiesString.split(";")
//
//                val cookies = HashMap<String, H.Cookie>()
//                for (cookieStr in cookieStrings) {
//                    val kvs = cookieStr.split("=")
//                    val cookie = H.Cookie()
//                    val key = kvs[0].trim()
//                    if (key == "YuSid") {
//                        val sid = kvs[1].trim()
//                        val psId = "${port}_$sid"
//                        session = cache[psId] ?: {
//                            val s = H.Session(sid, HashMap())
//                            cache[psId] = s
//                            s
//                        }()
//                        continue
//                    }
//                    cookie.key = key
//                    cookie.value = kvs[1].trim()
//                    cookies[key] = cookie
//                }
//                req.cookies = cookies
//            }
//
//            if (session == null) {
//                session = H.Session(UUID.randomUUID().toString(), HashMap())
//
//                response.addHeader("Set-Cookie", session.toCookie().toCookieString())
//            }
//
//            if (method == "POST") {
//                if ("application/json" == contentType) {
//                    val bufferSize = 1024
//                    val buffer = CharArray(bufferSize)
//                    val out = StringBuilder()
//                    val input = InputStreamReader(request.inputStream, request.characterEncoding)
//                    while (true) {
//                        val rsz = input.read(buffer, 0, buffer.size)
//                        if (rsz < 0)
//                            break
//                        out.append(buffer, 0, rsz)
//                    }
//                    req.body = out.toString()
//                }
//            }
//            req.para = request.parameters
//
//            val resp = H.Response()
//
//            val p = path.substring(1, path.length).split("/")
//            val context = WebActionContext(p.toTypedArray())
//
//            context.request = req
//            context.response = resp
//
//            try {
//                context.success = router.invoke(p[0], context)
//            } catch (e: Exception) {
//                e.printStackTrace()
//                response.httpStatus = HttpStatus.valueOf(500)
//                return
//            }
//            if (!context.success) {
//                response.httpStatus = HttpStatus.valueOf(404)
//                return
//            }
//
////            response.setContentType(resp.contentType)
//            val header = resp.header
//            if (header != null) {
//                for (key in header.keys) {
//                    response.addHeader(key, header[key])
//                }
//            }
//            val cookies = resp.getCookies()
//            if (cookies != null) {
//                for (cookie in cookies.iterator()) {
//                    response.addHeader("Set-Cookie", cookie.toCookieString())
//                }
//            }
//            response.write(resp.body?.toByteArray())
//        }
//    }

}

