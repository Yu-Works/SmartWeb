package com.IceCreamQAQ.SmartWeb.websocket

interface WsContext {

    val action: WsAction

    val sessions: List<WsSession>

    fun findSessionByName(name: String): WsSession? = sessions.firstOrNull { it.name == name }
    fun findSessionsByGroup(group: String): List<WsSession> = sessions.filter { it.group == group }

    fun sendToName(name: String, data: String) {
        sessions.firstOrNull { it.name == name }?.send(data)
    }

    fun sendToName(name: String, data: ByteArray) {
        sessions.firstOrNull { it.name == name }?.send(data)
    }

    fun sendToGroup(group: String, data: String) {
        sessions.asSequence().filter { it.group == group }.forEach { it.send(data) }
    }

    fun sendToGroup(group: String, data: ByteArray) {
        sessions.asSequence().filter { it.group == group }.forEach { it.send(data) }
    }

    fun sendToAll(data: String) {
        sessions.forEach { it.send(data) }
    }

    fun sendToAll(data: ByteArray) {
        sessions.forEach { it.send(data) }
    }

}