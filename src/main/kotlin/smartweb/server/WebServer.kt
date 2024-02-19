package smartweb.server

import smartweb.http.websocket.WsAction

interface WebServer {

    fun createWsAction(path: String, action: WsAction)
    
}