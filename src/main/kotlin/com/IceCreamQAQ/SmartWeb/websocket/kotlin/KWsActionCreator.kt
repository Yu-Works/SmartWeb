package com.IceCreamQAQ.SmartWeb.websocket.kotlin

import com.IceCreamQAQ.SmartWeb.WebServer
import com.IceCreamQAQ.SmartWeb.websocket.WsSession
import com.IceCreamQAQ.YuWeb.InternalWebServer

class KWsActionCreator {

    companion object {
        fun WebServer.newWs(path: String, create: KWsActionCreator.() -> Unit): Unit =
            createWsAction(path, KWsActionCreator().apply(create).build(this as InternalWebServer))
    }

    private var handShake: suspend WsSession.() -> Unit = {}
    private var close: suspend WsSession.() -> Unit = {}
    private var handleText: suspend WsSession.(String) -> Unit = {}
    private var handleByteArray: suspend WsSession.(ByteArray) -> Unit = {}
    private var error: suspend WsSession.(Throwable) -> Unit = {}

    fun handShake(handShake: suspend WsSession.() -> Unit) {
        this.handShake = handShake
    }

    fun close(close: suspend WsSession.() -> Unit) {
        this.close = close
    }

    fun handleText(handleText: suspend WsSession.(String) -> Unit) {
        this.handleText = handleText
    }

    fun handleByteArray(handleByteArray: suspend WsSession.(ByteArray) -> Unit) {
        this.handleByteArray = handleByteArray
    }

    fun error(error: suspend WsSession.(Throwable) -> Unit) {
        this.error = error
    }

    internal fun build(server: InternalWebServer): KWsAction =
        KWsAction(server.pool, handShake, close, handleText, handleByteArray, error)

}