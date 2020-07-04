package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.Yu.controller.NewControllerLoaderImpl
import com.IceCreamQAQ.Yu.controller.router.NewRouterImpl
import com.IceCreamQAQ.Yu.di.YuContext
import com.IceCreamQAQ.Yu.loader.LoadItem
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class WebControllerLoader : NewControllerLoaderImpl() {


    @Inject
    private lateinit var context: YuContext

    val rootRouters = HashMap<String, NewRouterImpl>()

    override fun load(items: Map<String, LoadItem>) {
        for (item in items.values) {
            val clazz = item.type
            val name = clazz.getAnnotation(Named::class.java)?.value
                    ?: item.type::class.java.interfaces[0].getAnnotation(Named::class.java)?.value ?: ""
            val rootRouter = rootRouters[name] ?: {
                val r = NewRouterImpl(0)
                rootRouters[name] = r
                r
            }()

            controllerToRouter(context[clazz] ?: continue, rootRouter)
        }
    }

}