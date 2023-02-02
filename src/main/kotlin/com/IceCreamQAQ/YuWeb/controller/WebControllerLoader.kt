package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.SmartWeb.annotation.NewWs
import com.IceCreamQAQ.SmartWeb.websocket.WsAction
import com.IceCreamQAQ.SmartWeb.websocket.kotlin.KWsActionCreator
import com.IceCreamQAQ.Yu.annotation
import com.IceCreamQAQ.Yu.annotation.After
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Default
import com.IceCreamQAQ.Yu.controller.*
import com.IceCreamQAQ.Yu.di.YuContext
import com.IceCreamQAQ.Yu.di.YuContext.Companion.get
import com.IceCreamQAQ.Yu.isBean
import com.IceCreamQAQ.Yu.loader.LoadItem
import com.IceCreamQAQ.YuWeb.controller.WebActionInvoker
import com.IceCreamQAQ.YuWeb.controller.WebReflectMethodInvoker
import com.IceCreamQAQ.YuWeb.controller.WebRootRouter
import com.IceCreamQAQ.YuWeb.temple.TempleEngine
import com.IceCreamQAQ.YuWeb.validation.ValidatorFactory
import java.lang.reflect.Method
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class WebControllerLoader : DefaultControllerLoaderImpl() {


    @Inject
    private lateinit var context: YuContext

    @Inject
    private lateinit var factory: ValidatorFactory

    @Inject
    private var templeEngine: TempleEngine? = null

    val rootRouters = HashMap<String, WebRootRouter>()

    @Config("web.temple.impl")
    private val impl: String? = null

    override fun load(items: Collection<LoadItem>) {
        impl?.let {
            context.getBean(TempleEngine::class.java, impl)?.let {
                it.start("dev")
                templeEngine = it
            }
        }

        val rootRouters = HashMap<String, RootRouter>()
        for (item in items) {
            if (!item.clazz.isBean()) continue
            val clazz = item.clazz
            val name = clazz.getAnnotation(Named::class.java)?.value
                ?: item.target.interfaces[0].getAnnotation(Named::class.java)?.value ?: ""
            val rootRouter = rootRouters.getOrPut(name) { RootRouter() }

            controllerToRouter(clazz, context[clazz] ?: continue, rootRouter)
            controllerFindWs(clazz, instance, rootRouter)
        }

        for ((k, v) in rootRouters) {
            v.router.init(v)
            this.rootRouters[k] = v
        }
    }

    fun controllerFindWs(controllerClass: Class<*>, instance: Any, rootRouter: WebRootRouter) {
        controllerClass.methods.forEach {
            it.annotation<NewWs> {
                if (it.parameters.isEmpty())
                    it.invoke(instance).also { r ->
                        when(r){
                            is WsAction -> rootRouter.wsList.add(value to r)
                            is KWsActionCreator -> rootRouter.kwsList.add(value to r)
                        }
                    }
            }
        }
    }

    override fun createMethodInvoker(instance: Any, method: Method) =
        WebReflectMethodInvoker(method, instance, null, factory)

    override fun createActionInvoker(level: Int, actionMethod: Method, instance: Any) =
        WebActionInvoker(
            level,
            actionMethod,
            instance,
            factory,
            templeEngine?.getTemple("${actionMethod.declaringClass.name}.${actionMethod.name}".replace(".", "/"))
        )


}