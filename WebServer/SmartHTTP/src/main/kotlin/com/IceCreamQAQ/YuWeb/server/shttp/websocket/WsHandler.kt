package com.IceCreamQAQ.YuWeb.server.shttp.websocket

import com.IceCreamQAQ.SmartWeb.websocket.WsAction
import com.IceCreamQAQ.SmartWeb.websocket.WsContext
import com.IceCreamQAQ.SmartWeb.websocket.WsSession
import com.IceCreamQAQ.YuWeb.server.shttp.SmartHTTPServer
import org.smartboot.http.server.WebSocketRequest
import org.smartboot.http.server.WebSocketResponse
import org.smartboot.http.server.handler.WebSocketDefaultHandler
import java.util.LinkedList

class WsHandler(val server: SmartHTTPServer, override val action: WsAction) : WebSocketDefaultHandler(), WsContext {

    override val sessions: MutableList<WsSession> = LinkedList<WsSession>()

    private fun session(req: WebSocketRequest, resp: WebSocketResponse): WsSession =
        if (req.getAttachment<WsSession>() == null)
            WsSessionImpl(this, WsReqImpl(req), WsRespImpl(resp)).apply { req.setAttachment(this) }
        else req.getAttachment()

    private fun doHandle(req: WebSocketRequest, resp: WebSocketResponse, body: (WsSession) -> Unit) {
        session(req, resp).apply {
            kotlin.runCatching {
                apply(body)
            }.getOrElse {
                action.onError(this, it)
            }
        }
    }

    override fun onHandShake(request: WebSocketRequest, response: WebSocketResponse) {
        doHandle(request, response) {
            sessions.add(it)
            action.onHandShake(it)
        }
    }

    override fun onClose(request: WebSocketRequest, response: WebSocketResponse) {
        doHandle(request, response) {
            sessions.remove(it)
            action.onClose(it)
        }
    }

    override fun handleTextMessage(request: WebSocketRequest, response: WebSocketResponse, data: String) {
        doHandle(request, response) {
            action.handleTextMessage(it, data)
        }
    }

    override fun handleBinaryMessage(request: WebSocketRequest, response: WebSocketResponse, data: ByteArray) {
        doHandle(request, response) {
            action.handleBinaryMessage(it, data)
        }
    }

}