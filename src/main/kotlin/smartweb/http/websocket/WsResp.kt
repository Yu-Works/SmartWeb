package smartweb.http.websocket

import java.io.Closeable

interface WsResp : Closeable {

    fun send(data: String)
    fun send(data: ByteArray)

}