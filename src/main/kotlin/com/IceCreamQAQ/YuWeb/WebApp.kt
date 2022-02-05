package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.Yu.`as`.ApplicationService
import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.IceCreamQAQ.Yu.di.ConfigManager
import com.IceCreamQAQ.Yu.di.ConfigManagerDefaultImpl
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.collections.ArrayList
import kotlin.properties.ReadOnlyProperty

class WebApp : ApplicationService {

    @Inject
    private lateinit var controllerLoader: WebControllerLoader

    @Inject
    private lateinit var configManager: ConfigManagerDefaultImpl

    @Inject
    @field:Named("WebSession")
    private lateinit var sessionCache: EhcacheHelp<H.Session>

//    private inline fun <reified T : Any> inject(name:String = ""): T {
//
//    }

    override fun init() {

    }

    private val servers = ArrayList<WebServer>()
    override fun start() {
        val rooters = controllerLoader.rootRouters

        for ((k, v) in rooters) {
            val configName = if (k == "") "webServer.port" else "webServer.$k.port"
            val corsName = if (k == "") "webServer.cors" else "webServer.$k.cors"

            val port =
                configManager.get(configName, String::class.java)?.toInt() ?: error("No Server: $k's Port Config!")
            val cors = configManager.get(corsName, String::class.java)

            val server = WebServer(port, cors, v, sessionCache) {
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