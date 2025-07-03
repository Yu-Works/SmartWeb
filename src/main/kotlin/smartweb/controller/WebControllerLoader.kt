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
import rain.controller.dss.DssControllerLoader
import rain.controller.dss.router.DssRouter
import rain.controller.dss.router.DynamicRouter
import rain.controller.dss.router.RouterMatcher
import rain.controller.simple.SimpleCatchMethodInvoker
import rain.di.Config
import rain.function.annotation
import rain.function.nameWithParamsFullClass
import java.lang.reflect.Method

class WebControllerLoader(
    @Config("smart.web.controller.contextValueKeys") val contextValueKeys: List<String> = emptyList(),
    context: DiContext,
    val templeEngines: List<TempleEngine> = arrayListOf()
) : DssControllerLoader<WebActionContext, WebRouter, WebRootInfo, WebActionInvoker>(context) {

    val rootInfoMap = HashMap<String, WebRootInfo>()
    val rootRouterMap = HashMap<String, WebRootRouter>()

    override fun findRootRouter(name: String): WebRootInfo =
        rootInfoMap.getOrPut(name) { WebRootInfo(DssRouter(0)) }

    override fun getSubStaticRouter(
        router: WebRouter,
        subPath: String
    ): WebRouter =
        router.static.getOrPut(subPath) {
            DssRouter(router.level + 1)
        }

    override fun getSubDynamicRouter(
        router: WebRouter,
        matcher: RouterMatcher<WebActionContext>
    ): WebRouter =
        let {
            router.dynamic.firstOrNull { it.matcher == matcher }
                ?: DynamicRouter(matcher, WebRouter(router.level + 1))
                    .apply { router.dynamic.add(this) }
        }.router

    override fun controllerChannel(annotation: Annotation?, controllerClass: Class<*>): List<String> =
        arrayListOf("GET", "POST", "PUT", "DELETE")

    override fun actionInfo(controllerChannel: List<String>, actionMethod: Method): Pair<String, List<String>>? {
        actionMethod.annotation<WebAction> { return value to methods.toList() }
        actionMethod.annotations
            .asSequence()
            .mapNotNull { it::class.java.interfaces.getOrNull(0)?.let { anc -> it to anc } }
            .forEach { (an, anClass) ->
                val reqMethods = anClass.annotation<RequestMethods>()
                if (reqMethods == null) return@forEach
                val ac = an::class.java
                val f = try {
                    ac.getMethod("value")
                }catch (e: NoSuchMethodException) {
                    error("Action ${actionMethod.nameWithParamsFullClass} 加载时遇到问题，提供的注解 ${anClass.name} 没有提供对应的 value 属性。")
                }
                val path = f.invoke(an)
                if (path !is String)
                    error("Action ${actionMethod.nameWithParamsFullClass} 加载时遇到问题，提供的注解 ${anClass.name} 的 value 属性不是 String 类型。")
                return path to reqMethods.value.toList()
            }
        actionMethod.annotation<NewWs> { return value to listOf("WebSocket") }
        return null
    }

    override fun putAction(router: WebRouter, channels: List<String>, actionInvoker: WebActionInvoker) {
        var mapping = router.action
        if (mapping == null) {
            mapping = ActionRequestMethodMapping()
            router.action = mapping
        }
        // 后续这里应该添加重复 Action 映射检测
        channels.forEach { method ->
            when (HttpMethod.valueOf(method)) {
                HttpMethod.GET -> mapping.getAction = actionInvoker
                HttpMethod.POST -> mapping.postAction = actionInvoker
                HttpMethod.PUT -> mapping.putAction = actionInvoker
                HttpMethod.DELETE -> mapping.deleteAction = actionInvoker
                else -> mapping.actionMap[method] = actionInvoker
            }
        }
    }

    override fun createActionInvoker(
        channels: List<String>,
        actionClass: Class<*>,
        actionMethod: Method,
        instanceGetter: ControllerInstanceGetter,
        beforeProcesses: Array<ProcessInvoker<WebActionContext>>,
        afterProcesses: Array<ProcessInvoker<WebActionContext>>,
        catchProcesses: Array<ProcessInvoker<WebActionContext>>
    ) = WebActionInvoker(
        channels.map { HttpMethod.valueOf(it) },
        "${actionClass.name.replace(".", "/")}/${actionMethod.name}.html".let { templePath ->
            templeEngines.forEachFirstOrNull { it.getTemple(templePath) }
        },
        WebMethodInvoker(actionMethod, instanceGetter, contextValueKeys).init(),
        beforeProcesses,
        afterProcesses,
        catchProcesses
    )


    override fun createMethodInvoker(
        controllerClass: Class<*>,
        targetMethod: Method,
        instanceGetter: ControllerInstanceGetter
    ): ProcessInvoker<WebActionContext> =
        WebMethodInvoker(targetMethod, instanceGetter, contextValueKeys).init()

    override fun createCatchMethodInvoker(
        throwableType: Class<out Throwable>,
        controllerClass: Class<*>,
        targetMethod: Method,
        instanceGetter: ControllerInstanceGetter
    ): ProcessInvoker<WebActionContext> =
        SimpleCatchMethodInvoker(throwableType, WebMethodInvoker(targetMethod, instanceGetter, contextValueKeys).init())


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