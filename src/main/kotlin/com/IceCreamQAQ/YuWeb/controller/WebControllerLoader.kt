package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.Yu.annotation.After
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.controller.DefaultControllerLoaderImpl
import com.IceCreamQAQ.Yu.controller.RootRouter
import com.IceCreamQAQ.Yu.controller.Router
import com.IceCreamQAQ.Yu.di.YuContext
import com.IceCreamQAQ.Yu.isBean
import com.IceCreamQAQ.Yu.loader.LoadItem
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class WebControllerLoader : DefaultControllerLoaderImpl() {


    @Inject
    private lateinit var context: YuContext

    val rootRouters = HashMap<String, Router>()

    override fun load(items: Map<String, LoadItem>) {
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
        val rootRouters = HashMap<String, RootRouter>()
        for (item in items.values) {
            if (!item.type.isBean()) continue
            val clazz = item.type
            val name = clazz.getAnnotation(Named::class.java)?.value
                    ?: item.loadBy::class.java.interfaces[0].getAnnotation(Named::class.java)?.value ?: ""
            val rootRouter = rootRouters[name] ?: {
                val r = RootRouter()
                rootRouters[name] = r
                r
            }()

            controllerToRouter(context[clazz] ?: continue, rootRouter)
        }

        for ((k, v) in rootRouters) {
            v.router.init(v)
            this.rootRouters[k] = v.router
        }
    }

}