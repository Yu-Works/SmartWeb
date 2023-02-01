package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.SmartWeb.WebServer
import com.IceCreamQAQ.SmartWeb.event.WebServerStatusChangedEvent
import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.`as`.ApplicationService
import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.IceCreamQAQ.Yu.di.YuContext
import com.IceCreamQAQ.Yu.di.config.ConfigManager
import com.IceCreamQAQ.Yu.event.EventBus
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.collections.ArrayList

class WebApp : ApplicationService {

    val isDev = file("pom.xml", "build.gradle", "build.gradle.kts") != null


    @Inject
    private lateinit var controllerLoader: WebControllerLoader

    @Inject
    private lateinit var configManager: ConfigManager

    @Inject
    @field:Named("WebSession")
    private lateinit var sessionCache: EhcacheHelp<H.Session>

    @Inject
    private lateinit var context: YuContext

    @Config("yu.web.server.default")
    private lateinit var defaultImpl: String

    @Inject
    private lateinit var eventBus: EventBus

    override fun init() {

    }

    private val servers = ArrayList<InternalWebServer>()
    override fun start() {
        val rooters = controllerLoader.rootRouters

        for ((k, v) in rooters) {
            val configName = if (k == "") "webServer.port" else "webServer.$k.port"
            val corsName = if (k == "") "webServer.cors" else "webServer.$k.cors"
            val serverImplName = if (k == "") "webServer.impl" else "webServer.$k.impl"

            val port =
                configManager.getConfig(configName, String::class.java)?.toInt() ?: error("No Server: $k's Port Config!")
            val cors = configManager.getConfig(corsName, String::class.java)
            val serverImpl = Class.forName(
                configManager.getConfig(serverImplName, String::class.java) ?: defaultImpl
            )

            val server = (context.newBean(serverImpl) as InternalWebServer)
                .isDev(isDev)
                .name(k)
                .port(port)
                .corsStr(cors)
                .router(v)
                .eventBus(eventBus)
                .sessionCache(sessionCache)
                .createSession {
                    val sid = UUID.randomUUID().toString()
                    val psId = "${port}_$sid"
                    val session = H.Session(psId, HashMap())
                    sessionCache[psId] = session
                    session
                }

            context.putBean(WebServer::class.java, configName, server)
            servers.add(server)
            server.start()
            eventBus.post(WebServerStatusChangedEvent.Started(server))
        }
    }

    override fun stop() {
        for (server in servers) {
            eventBus.post(WebServerStatusChangedEvent.Stopping(server))
            server.stop()
        }
    }
}