package smartweb.server.shttp

import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import org.smartboot.http.server.HttpRequest
import java.io.InputStream
import java.net.InetSocketAddress

class Req(private val request: HttpRequest) : smartweb.http.Request {
    override val scheme: String
        get() = header("X-Forwarded-Proto")?.value ?: request.scheme
    override val host: String
        get() = request.getHeader("Host")!!
    override val url: String
        get() = request.requestURL
    override val path: String
        get() = request.requestURI
    override val method: String = request.method.toUpperCase()
    override val headers: Array<smartweb.http.Header> = ArrayList<smartweb.http.Header>().apply {
        request.headerNames.forEach { k ->
            request.getHeaders(k).forEach { v -> add(smartweb.http.Header(k, v)) }
        }
    }.toTypedArray()
    override val contentType: String
        get() = (request.contentType ?: "").split(";")[0].trim()
    override val charset: String
        get() = request.characterEncoding
    override val queryString: String?
        get() = request.queryString

    override val parameters: Map<String, Array<String>>
        get() = request.parameters
    override val cookies: Array<smartweb.http.Cookie> = request.cookies.map { smartweb.http.Cookie(it.name, it.value) }.toTypedArray()

    override var body: JSONObject? = null
    override var bodyArray: JSONArray? = null
    override var uploadFiles: Map<String, ArrayList<smartweb.http.UploadFile>>? = null

    override val inputStream: InputStream? = null

    override val userAgent: String
        get() = request.getHeader("User-Agent")
    override val userAddress: Array<String>
        get() = TODO("Not yet implemented")
    override val clientAddress: InetSocketAddress
        get() = TODO("Not yet implemented")
    override var cors: smartweb.http.CORS? = null

    override val accept: smartweb.http.Accept = smartweb.http.Accept(
        (request.getHeader("Accept") ?: "*/*").split(",").map { it.trim() }.toTypedArray(),
        "",
        "",
        ""
    )
    override lateinit var session: smartweb.http.Session

    override fun getParameterValues(name: String): Array<String>? = parameters[name]
}
