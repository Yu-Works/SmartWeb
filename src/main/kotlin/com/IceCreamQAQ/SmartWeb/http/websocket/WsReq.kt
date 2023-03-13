package com.IceCreamQAQ.SmartWeb.http.websocket

import java.net.InetAddress
import java.net.InetSocketAddress

interface WsReq {

    val frameOpcode: Int
    val payload: ByteArray

    val path: String

    val queryString: String
    val parameters: Map<String, Array<String>>

    val remoteAddress: InetSocketAddress
    val localAddress: InetSocketAddress

}