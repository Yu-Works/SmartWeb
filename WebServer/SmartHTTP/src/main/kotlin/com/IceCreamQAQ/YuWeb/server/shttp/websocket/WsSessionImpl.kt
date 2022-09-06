package com.IceCreamQAQ.YuWeb.server.shttp.websocket

import com.IceCreamQAQ.SmartWeb.websocket.WsContext
import com.IceCreamQAQ.SmartWeb.websocket.WsReq
import com.IceCreamQAQ.SmartWeb.websocket.WsResp
import com.IceCreamQAQ.SmartWeb.websocket.WsSession

class WsSessionImpl(
    override val context: WsContext,
    override val req: WsReqImpl,
    override val resp: WsResp
) : WsSession {
    override var attachment: Any? = null
    override var name: String? = null
    override var group: String? = null
}