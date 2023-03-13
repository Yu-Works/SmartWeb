package com.IceCreamQAQ.SmartWeb.server

import com.IceCreamQAQ.SmartWeb.http.websocket.WsAction

interface WebServer {

    fun createWsAction(path: String, action: WsAction)
    
}