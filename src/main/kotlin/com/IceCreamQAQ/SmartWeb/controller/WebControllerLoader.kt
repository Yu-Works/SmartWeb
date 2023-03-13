package com.IceCreamQAQ.SmartWeb.controller

import com.IceCreamQAQ.SmartWeb.annotation.WebAction
import com.IceCreamQAQ.SmartWeb.forEachFirst
import com.IceCreamQAQ.SmartWeb.forEachFirstOrNull
import com.IceCreamQAQ.SmartWeb.http.HttpMethod
import com.IceCreamQAQ.SmartWeb.temple.TempleEngine
import com.IceCreamQAQ.Yu.annotation
import com.IceCreamQAQ.Yu.controller.*
import com.IceCreamQAQ.Yu.controller.dss.DssActionInvoker
import com.IceCreamQAQ.Yu.controller.dss.DssControllerLoader
import com.IceCreamQAQ.Yu.controller.dss.router.DssRouter
import com.IceCreamQAQ.Yu.controller.dss.router.DynamicRouter
import com.IceCreamQAQ.Yu.controller.dss.router.RouterMatcher
import com.IceCreamQAQ.Yu.di.YuContext
import java.lang.reflect.Method

class WebControllerLoader(
    context: YuContext,
    val templeEngines: List<TempleEngine> = arrayListOf()
) : DssControllerLoader<WebActionContext, WebRouter, WebRootInfo>(
    context
) {

    val rootInfoMap = HashMap<String, WebRootInfo>()
    val rootRouterMap = HashMap<String, WebRootRouter>()

    override fun findRootRouter(name: String): WebRootInfo =
        rootInfoMap.getOrPut(name) { WebRootInfo(DssRouter(0)) }

    override fun getSubStaticRouter(
        router: DssRouter<WebActionContext>,
        subPath: String
    ): DssRouter<WebActionContext> =
        router.staticSubrouter.getOrPut(subPath) {
            DssRouter(router.level + 1)
        }

    override fun getSubDynamicRouter(
        router: DssRouter<WebActionContext>,
        matcher: RouterMatcher<WebActionContext>
    ): DssRouter<WebActionContext> =
        let {
            router.dynamicSubrouter.firstOrNull { it.matcher == matcher }
                ?: DynamicRouter(matcher, DssRouter(router.level + 1))
                    .apply { router.dynamicSubrouter.add(this) }
        }.router

    override fun controllerChannel(annotation: Annotation?, controllerClass: Class<*>): List<String> =
        arrayListOf("GET", "POST", "PUT", "DELETE")

    override fun actionInfo(controllerChannel: List<String>, actionMethod: Method): Pair<String, List<String>>? {
        actionMethod.annotation<WebAction> { return value to methods.map { it.method } }
        actionMethod.annotation<WebAction> { return value to listOf("test1") }
        actionMethod.annotation<WebAction> { return value to listOf("test2") }
        actionMethod.annotation<WebAction> { return value to listOf("test3") }
        actionMethod.annotation<WebAction> { return value to listOf("test4") }
        return null
    }

    override fun createActionInvoker(
        channels: List<String>,
        level: Int,
        matchers: List<RouterMatcher<WebActionContext>>,
        actionClass: Class<*>,
        actionMethod: Method,
        instanceGetter: ControllerInstanceGetter,
        beforeProcesses: Array<ProcessInvoker<WebActionContext>>,
        afterProcesses: Array<ProcessInvoker<WebActionContext>>,
        catchProcesses: Array<ProcessInvoker<WebActionContext>>
    ): DssActionInvoker<WebActionContext> =
        WebActionInvoker(
            channels.map { HttpMethod.valueOf(it) },
            "${actionClass.name.replace(".", "/")}/${actionMethod.name}.html".let { templePath ->
                templeEngines.forEachFirstOrNull { it.getTemple(templePath) }
            },
            level,
            matchers,
            WebMethodInvoker(actionMethod, instanceGetter),
            beforeProcesses,
            afterProcesses,
            catchProcesses
        )


    override fun createMethodInvoker(
        controllerClass: Class<*>,
        targetMethod: Method,
        instanceGetter: ControllerInstanceGetter
    ): ProcessInvoker<WebActionContext> =
        WebMethodInvoker(targetMethod, instanceGetter)

    override fun createCatchMethodInvoker(
        throwableType: Class<out Throwable>,
        controllerClass: Class<*>,
        targetMethod: Method,
        instanceGetter: ControllerInstanceGetter
    ): ProcessInvoker<WebActionContext> =
        WebCatchMethodInvoker(targetMethod, instanceGetter, throwableType)


    override fun postLoad() {
        rootRouterMap.apply {
            rootInfoMap.forEach { name, rootInfo ->
                put(name, RootRouter(rootInfo.router, ArrayList<ActionInfo<WebActionContext>>().apply {
                    rootInfo.controllers.forEach { cpfi ->
                        cpfi.actions.forEach { add(ActionInfo(it.actionClass, it.actionMethod, it.creator())) }
                    }
                }))
            }
        }
    }


}