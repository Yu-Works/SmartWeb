package com.IceCreamQAQ.YuWeb.server.shttp.websocket

import com.IceCreamQAQ.SmartWeb.websocket.WsContext
import com.IceCreamQAQ.SmartWeb.websocket.WsReq
import com.IceCreamQAQ.SmartWeb.websocket.WsResp
import com.IceCreamQAQ.SmartWeb.websocket.WsSession

class WsSessionImpl(
    override val context: WsContext,
    override val req: WsReq,
    override val resp: WsResp
) : WsSession {
    override var name: String? = null
    override var group: String? = null
}