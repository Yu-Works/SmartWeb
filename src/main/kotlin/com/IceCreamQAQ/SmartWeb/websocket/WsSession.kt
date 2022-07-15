package com.IceCreamQAQ.SmartWeb.websocket

interface WsSession {

    val context: WsContext

    val req: WsReq
    val resp: WsResp

    var name: String?
    var group: String?

    fun send(data: String) {
        resp.send(data)
    }

    fun send(data: ByteArray) {
        resp.send(data)
    }

    fun sendToName(name: String, data: String) {
        context.sendToName(name, data)
    }

    fun sendToName(name: String, data: ByteArray) {
        context.sendToName(name, data)
    }

    fun sendToGroup(group: String, data: String) {
        context.sendToName(group, data)
    }

    fun sendToGroup(group: String, data: ByteArray) {
        context.sendToName(group, data)
    }

    fun sendToAll(data: String) {
        context.sendToAll(data)
    }

    fun sendToAll(data: ByteArray) {
        context.sendToAll(data)
    }

}