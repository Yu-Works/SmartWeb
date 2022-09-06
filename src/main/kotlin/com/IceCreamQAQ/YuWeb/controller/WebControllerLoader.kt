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
import com.IceCreamQAQ.Yu.fullName
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
    @Default("")
    private lateinit var impl: String

    override fun load(items: Map<String, LoadItem>) {
        context.getBean(TempleEngine::class.java, impl)?.let {
            it.start("dev")
            templeEngine = it
        }
//        for (item in items.values) {
//            val clazz = item.type
//            val name = clazz.getAnnotation(Named::class.java)?.value
//                    ?: item.type::class.java.interfaces[0].getAnnotation(Named::class.java)?.value ?: ""
//            val rootRouter = rootRouters[name] ?: {
//                val r = NewRouterImpl(0)
//                rootRouters[name] = r
//                r
//            }()
//
//            controllerToRouter(context[clazz] ?: continue, rootRouter)
//        }
//        val rootRouters = HashMap<String, WebRootRouter>()
        for (item in items.values) {
            if (!item.type.isBean()) continue
            val clazz = item.type
            val name = clazz.getAnnotation(Named::class.java)?.value
                ?: item.loadBy::class.java.interfaces[0].getAnnotation(Named::class.java)?.value ?: ""
            val rootRouter = rootRouters.getOrPut(name) { WebRootRouter() }

            val instance = context[clazz] ?: continue
            controllerToRouter(clazz, instance, rootRouter)
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