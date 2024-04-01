package smartweb.controller

import smartweb.annotation.*
import smartweb.forEachFirstOrNull
import smartweb.http.HttpMethod
import smartweb.temple.TempleEngine
import rain.api.di.DiContext
import rain.controller.ActionInfo
import rain.controller.ControllerInstanceGetter
import rain.controller.ProcessInvoker
import rain.controller.RootRouter
import rain.controller.dss.DssActionInvoker
import rain.controller.dss.DssControllerLoader
import rain.controller.dss.router.DssRouter
import rain.controller.dss.router.DynamicRouter
import rain.controller.dss.router.RouterMatcher
import rain.function.annotation
import java.lang.reflect.Method

class WebControllerLoader(
    context: DiContext,
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
        actionMethod.annotation<GetAction> { return value to listOf("GET") }
        actionMethod.annotation<PostAction> { return value to listOf("POST") }
        actionMethod.annotation<PutAction> { return value to listOf("PUT") }
        actionMethod.annotation<DeleteAction> { return value to listOf("DELETE") }
        actionMethod.annotation<NewWs> { return value to  listOf("WebSocket") }
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
            rootInfoMap.forEach { (name, rootInfo) ->
                put(name, RootRouter(rootInfo.router, ArrayList<ActionInfo<WebActionContext>>().apply {
                    rootInfo.controllers.forEach { cpfi ->
                        cpfi.actions.forEach { add(ActionInfo(it.actionClass, it.actionMethod, it.creator())) }
                    }
                }))
            }
        }
    }


}