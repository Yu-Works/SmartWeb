package com.IceCreamQAQ.YuWeb.server.shttp.websocket

import com.IceCreamQAQ.SmartWeb.websocket.WsResp
import org.smartboot.http.server.WebSocketResponse

class WsRespImpl(private val real: WebSocketResponse) : WsResp {
    override fun send(data: String) {
        real.sendTextMessage(data)
        real.flush()
    }

    override fun send(data: ByteArray) {
        real.sendBinaryMessage(data)
        real.flush()
    }

    override fun close() {
        real.close()
    }
}