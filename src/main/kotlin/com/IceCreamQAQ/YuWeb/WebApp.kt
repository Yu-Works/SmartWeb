package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.Yu.`as`.ApplicationService
import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.IceCreamQAQ.Yu.di.ConfigManager
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.collections.ArrayList

class WebApp : ApplicationService {

    @Inject
    private lateinit var controllerLoader: WebControllerLoader

    @Inject
    private lateinit var configManager: ConfigManager

    @Inject
    @field:Named("WebSession")
    private lateinit var sessionCache: EhcacheHelp<H.Session>

    override fun init() {

    }

    private val servers = ArrayList<WebServer>()
    override fun start() {
        val rooters = controllerLoader.rootRouters

        for ((k, v) in rooters) {
            val configName = if (k == "") "webServer.port" else "webServer.$k.port"
            val port = configManager.get(configName, String::class.java)?.toInt()
                    ?: error("No Server: $k's Port Config!")
            val server = WebServer(port, v, sessionCache) {
                val sid = UUID.randomUUID().toString()
                val psId = "${port}_$sid"
                val session = H.Session(psId, HashMap())
                sessionCache[psId] = session
                session
            }
            servers.add(server)
            server.start()
        }
    }

    override fun stop() {
        for (server in servers) {
            server.stop()
        }
    }
}