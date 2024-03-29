package smartweb.server.undertow.http

import smartweb.http.*
import io.undertow.server.HttpServerExchange
import io.undertow.util.HttpString
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

class Resp(val exc: HttpServerExchange) : Response {

    companion object {
        private val contentTypeHS = HttpString("Content-Type")
        private val setCookieHS = HttpString("Set-Cookie")
    }

    override var status: Int = 200
    override val cookies = ArrayList<Cookie>()
    override val headers = ArrayList<Header>()
    override var contentType: String? = null
    override var charset: String? = null
    override var contentLength: Long = -1

    override fun addCookie(cookie: Cookie) {
        cookies.add(cookie)
    }

    override fun addHeader(header: Header) {
        headers.add(header)
    }

    override fun addHeader(key: String, value: String) {
        headers.add(Header(key, value))
    }

    override val output: OutputStream
        get() {
            write()
            return exc.startBlocking().outputStream
        }

    override fun write() {
        this.contentType?.let { exc.responseHeaders.addLast(contentTypeHS, it) }

        headers.forEach { exc.responseHeaders.addLast(HttpString(it.name), it.value) }
        cookies.forEach { exc.responseHeaders.addLast(setCookieHS, it.toCookieString()) }

        exc.statusCode = this.status
        if (this.contentLength > 0) exc.responseContentLength = this.contentLength
    }

    override fun write(result: ByteArray) {
        write()
        exc.responseSender.send(ByteBuffer.wrap(result))
    }

    override fun write(result: InputStream) {
        write()
        result.use { it.copyTo(exc.startBlocking().outputStream) }
    }

}