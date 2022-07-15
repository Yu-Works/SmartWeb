package com.IceCreamQAQ.YuWeb.server.shttp.websocket

import com.IceCreamQAQ.SmartWeb.websocket.WsReq
import org.smartboot.http.server.WebSocketRequest
import java.net.InetSocketAddress

class WsReqImpl(private val real: WebSocketRequest) : WsReq {

    override val frameOpcode: Int
        get() = real.frameOpcode
    override val payload: ByteArray
        get() = real.payload
    override val path: String
        get() = real.requestURI
    override val queryString: String
        get() = real.queryString
    override val parameters: Map<String, Array<String>>
        get() = real.parameters
    override val remoteAddress: InetSocketAddress
        get() = real.remoteAddress
    override val localAddress: InetSocketAddress
        get() = real.localAddress

}