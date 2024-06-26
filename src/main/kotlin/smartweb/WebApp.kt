package smartweb

import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import rain.api.di.DiContext
import rain.api.di.DiContext.Companion.get
import rain.api.loader.ApplicationService
import rain.di.Config
import rain.di.config.ConfigManager
import rain.di.config.ConfigManager.Companion.getConfig
import rain.event.EventBusImpl
import rain.function.annotation
import smartweb.annotation.NewWs
import smartweb.controller.WebControllerLoader
import smartweb.event.WebServerStatusChangedEvent
import smartweb.http.websocket.WsAction
import smartweb.http.websocket.kotlin.KWsActionCreator
import smartweb.server.*
import java.time.Duration
import javax.inject.Named

class WebApp(
    val context: DiContext,
    val configManager: ConfigManager,
    val controllerLoader: WebControllerLoader,
    val eventBus: EventBusImpl,
    @Config("rain.runMode")
    runMode: String?,
    @Config("yu.web.server.default")
    val defaultImpl: String,
    @Named("{smartweb.userProvider}")
    val userProvider: WebUserProvider?
) : ApplicationService {

    val server = HashMap<String, InternalWebServer>()

    val isDevMode: Boolean

    private var cm: CacheManager? = null

    init {
        isDevMode = "dev" == runMode
    }

    override fun start() {
        var cmb = CacheManagerBuilder.newCacheManagerBuilder()


        controllerLoader.rootRouterMap.keys.forEach { name ->
            val serverName = name.ifEmpty { "default" }
            val configName = if (name.isEmpty()) "webServer" else "webServer.$name"

            cmb = cmb.withCache(
                serverName,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                    String::class.java,
                    Any::class.java,
                    ResourcePoolsBuilder
                        .heap(configManager.getConfig("$configName.sessionNum") ?: 2000)
                        .build()
                ).withExpiry(
                    ExpiryPolicyBuilder.timeToIdleExpiration(
                        Duration.ofSeconds(configManager.getConfig("$configName.sessionNum") ?: 1800)
                    )
                ).build()
            )
        }


        cm = cmb.build()
        cm!!.init()

        server.apply {
            controllerLoader.rootRouterMap
                .forEach { name, rootRouter ->
                    val serverName = name.ifEmpty { "default" }
                    val configName = if (name.isEmpty()) "webServer" else "webServer.$name"

                    val config = WebServerConfig(
                        isDevMode,
                        serverName,
                        configManager.getConfig("$configName.port")
                            ?: error("在试图构建 WebServer: $serverName 时，缺少必要的端口配置，请检查配置项: $configName.port。"),
                        configManager.getConfig("$configName.cors"),
                        configManager.getConfig<WebServerUploadConfig>("$configName.upload") ?: WebServerUploadConfig(),
                        rootRouter,
                        EhcacheHelp(cm!!.getCache(serverName, String::class.java, Any::class.java)),
                        userProvider
                    )
                    val impl = configManager.getConfig<String>("$configName.impl") ?: defaultImpl

                    val serverImpl = Class.forName(impl).getConstructor(WebServerConfig::class.java)
                        .newInstance(config) as InternalWebServer

                    rootRouter.actions
                        .forEach {
                            it.actionMethod.annotation<NewWs> {
                                serverImpl.createWsAction(
                                    value,
                                    it.actionMethod.run {
                                        invoke(context[declaringClass])
                                    }.let { r ->
                                        if (r is KWsActionCreator) r.build(serverImpl) else r as WsAction
                                    }
                                )
                            }
                        }

                    put(serverName, serverImpl)

                    serverImpl.start()
                    eventBus.post(WebServerStatusChangedEvent.Started(serverImpl))
                }
        }


    }

    override fun stop() {
        server.values.forEach {
            eventBus.post(WebServerStatusChangedEvent.Stopping(it))
            it.stop()
        }

    }
}