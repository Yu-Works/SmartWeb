package com.IceCreamQAQ.YuWeb.server.shttp.websocket

import smartweb.http.websocket.WsContext
import smartweb.http.websocket.WsReq
import smartweb.http.websocket.WsResp
import smartweb.http.websocket.WsSession

class WsSessionImpl(
    override val context: WsContext,
    override val req: WsReqImpl,
    override val resp: WsResp
) : WsSession {
    override var attachment: Any? = null
    override var name: String? = null
    override var group: String? = null
}