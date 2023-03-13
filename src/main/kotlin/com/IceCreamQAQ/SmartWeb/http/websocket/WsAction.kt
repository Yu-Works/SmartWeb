package com.IceCreamQAQ.SmartWeb.http.websocket

interface WsAction {

    /**
     * 链接握手
     *
     * @param session WebSocket 会话
     */
    fun onHandShake(session: WsSession)

    /**
     * 连接关闭
     *
     * @param session WebSocket 会话
     */
    fun onClose(session: WsSession)

    /**
     * 处理字符串请求消息
     *
     * @param session WebSocket 会话
     * @param data 字符串消息数据
     */
    fun handleTextMessage(session: WsSession, data: String)

    /**
     * 处理二进制请求消息
     *
     * @param session WebSocket 会话
     * @param data 二进制消息数据
     */
    fun handleBinaryMessage(session: WsSession, data: ByteArray)

    /**
     * 连接异常
     *
     * @param session WebSocket 会话
     * @param throwable
     */
    fun onError(session: WsSession, throwable: Throwable)
}