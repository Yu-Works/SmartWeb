package com.IceCreamQAQ.SmartWeb.http.websocket.kotlin

import com.IceCreamQAQ.SmartWeb.http.websocket.WsAction
import com.IceCreamQAQ.SmartWeb.http.websocket.WsSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class KWsAction(
    private val pool: CoroutineScope,
    val handShake: suspend WsSession.() -> Unit,
    val close: suspend WsSession.() -> Unit,
    val handleText: suspend WsSession.(String) -> Unit,
    val handleByteArray: suspend WsSession.(ByteArray) -> Unit,
    val error: suspend WsSession.(Throwable) -> Unit
) : WsAction {

    override fun onHandShake(session: WsSession) {
        pool.launch { handShake(session) }
    }

    override fun onClose(session: WsSession) {
        pool.launch { close(session) }
    }

    override fun handleTextMessage(session: WsSession, data: String) {
        pool.launch { handleText(session, data) }
    }

    override fun handleBinaryMessage(session: WsSession, data: ByteArray){
        pool.launch { handleByteArray(session, data) }
    }

    override fun onError(session: WsSession, throwable: Throwable){
        pool.launch { error(session, throwable) }
    }
}