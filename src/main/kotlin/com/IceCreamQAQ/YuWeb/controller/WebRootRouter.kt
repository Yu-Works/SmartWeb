package com.IceCreamQAQ.YuWeb.controller

import com.IceCreamQAQ.SmartWeb.websocket.WsAction
import com.IceCreamQAQ.SmartWeb.websocket.kotlin.KWsActionCreator
import com.IceCreamQAQ.Yu.controller.RootRouter

class WebRootRouter : RootRouter() {

    val wsList = ArrayList<Pair<String, WsAction>>()
    val kwsList = ArrayList<Pair<String, KWsActionCreator>>()

}