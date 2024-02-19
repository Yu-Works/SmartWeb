package com.IceCreamQAQ.SmartWeb.server

import com.IceCreamQAQ.SmartWeb.controller.WebRootRouter
import com.IceCreamQAQ.SmartWeb.http.Session

data class WebServerConfig(
    val isDevMode: Boolean,
    val name: String,
    val port: Int,
    val cors: String?,
    val upload: WebServerUploadConfig,
    val rootRouter: WebRootRouter,
    val sessionCache: EhcacheHelp<Session>
)