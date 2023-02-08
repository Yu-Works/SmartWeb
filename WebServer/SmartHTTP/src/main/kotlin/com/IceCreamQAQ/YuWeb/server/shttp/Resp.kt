package com.IceCreamQAQ.YuWeb.server.shttp

import com.IceCreamQAQ.YuWeb.H
import org.smartboot.http.common.enums.HttpStatus
import org.smartboot.http.server.HttpResponse
import java.io.InputStream
import java.io.OutputStream

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
        this.contentType?.let { response.contentType = it }

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
