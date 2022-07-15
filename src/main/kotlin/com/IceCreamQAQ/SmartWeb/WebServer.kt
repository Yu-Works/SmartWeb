package com.IceCreamQAQ.SmartWeb

import com.IceCreamQAQ.SmartWeb.websocket.kotlin.KWsActionCreator
import com.IceCreamQAQ.SmartWeb.websocket.WsAction
import kotlinx.coroutines.CoroutineScope

interface WebServer {

    fun createWsAction(path: String, action: WsAction)

}