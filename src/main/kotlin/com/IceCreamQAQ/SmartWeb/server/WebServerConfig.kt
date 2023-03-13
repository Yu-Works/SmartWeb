package com.IceCreamQAQ.SmartWeb.server

import com.IceCreamQAQ.SmartWeb.LazyObject
import com.IceCreamQAQ.SmartWeb.controller.WebRootRouter
import com.IceCreamQAQ.SmartWeb.http.Session
import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import org.ehcache.CacheManager

data class WebServerConfig(
    val isDevMode: Boolean,
    val name: String,
    val port: Int,
    val cors: String?,
    val rootRouter: WebRootRouter,
    val sessionCache: EhcacheHelp<Session>
)