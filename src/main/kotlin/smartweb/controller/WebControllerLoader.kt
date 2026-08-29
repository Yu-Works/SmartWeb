package smartweb.controller

import rain.api.di.DiContext
import rain.controller.ControllerInstanceGetter
import rain.controller.ProcessInvoker
import rain.controller.dss.DssControllerLoader
import rain.controller.dss.router.RouterMatcher
import rain.di.Config
import rain.function.annotation
import rain.function.nameWithParamsFullClass
import smartweb.annotation.ContextValues
import smartweb.annotation.NewWs
import smartweb.annotation.RequestMethods
import smartweb.annotation.WebAction
import smartweb.controller.router.WebDynamicRouter
import smartweb.controller.router.WebStaticRouter
import smartweb.forEachFirstOrNull
import smartweb.http.HttpMethod
import smartweb.temple.TempleEngine
import java.lang.reflect.Method

class WebControllerLoader(
    @Config("smart.web.controller.contextValueKeys") val contextValueKeys: List<String> = emptyList(),
    context: DiContext,
    val templeEngines: List<TempleEngine> = arrayListOf()
) : DssControllerLoader<WebActionContext, WebRouter, WebRootInfo, WebActionInvoker, WebControllerProcessFlowInfo>(
    context
) {

    val rootInfoMap = HashMap<String, WebRootInfo>()
    val rootRouterMap = HashMap<String, WebRootRouter>()

    override fun findRootRouter(name: String): WebRootInfo =
        rootInfoMap.getOrPut(name) { WebRootInfo(WebStaticRouter(0)) }

    override fun controllerInfo(
        controllerClass: Class<*>,
        controllerInstance: Any,
        controllerChannels: List<String>,
        controllerRouter: WebRouter
    ): WebControllerProcessFlowInfo = WebControllerProcessFlowInfo(
        controllerClass.annotation<ContextValues>()?.let { contextValueKeys + it.value } ?: contextValueKeys,
        controllerClass,
        controllerInstance,
        controllerChannels,
        controllerRouter
    )

    override fun getSubStaticRouter(
        router: WebRouter,
        subPath: String
    ): WebRouter =
        router.static.getOrPut(subPath) {
            WebStaticRouter(router.level + 1, router.pathVars)
        }

    override fun getSubDynamicRouter(
        router: WebRouter,
        matcher: RouterMatcher<WebActionContext>,
        names: List<String>
    ) = let {
        router.dynamic.firstOrNull { it.matcher == matcher }
            ?: WebDynamicRouter(matcher, WebStaticRouter(router.level + 1, pathVars = router.pathVars + names))
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
                } catch (e: NoSuchMethodException) {
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
            when (method) {
                "GET" -> mapping.getAction = actionInvoker
                "POST" -> mapping.postAction = actionInvoker
                "PUT" -> mapping.putAction = actionInvoker
                "DELETE" -> mapping.deleteAction = actionInvoker
                "PATCH" -> mapping.patchAction = actionInvoker
                else -> mapping.actionMap[method] = actionInvoker
            }
        }
    }

    override fun createActionInvoker(
        channels: List<String>,
        controllerInfo: WebControllerProcessFlowInfo,
        actionClass: Class<*>,
        actionMethod: Method,
        instanceGetter: ControllerInstanceGetter,
        actionRouter: WebRouter,
        beforeProcesses: Array<ProcessInvoker<WebActionContext>>,
        afterProcesses: Array<ProcessInvoker<WebActionContext>>,
        catchProcesses: Array<ProcessInvoker<WebActionContext>>
    ) = WebActionInvoker(
        actionClass,
        actionMethod,
        "${actionClass.name.replace(".", "/")}/${actionMethod.name}.html".let { templePath ->
            templeEngines.forEachFirstOrNull { it.getTemple(templePath) }
        },
        WebMethodInvoker(
            actionMethod,
            instanceGetter,
            controllerInfo.controllerContextValueNames,
            actionRouter.pathVars
        ).init(),
        beforeProcesses,
        afterProcesses,
        catchProcesses
    )


    override fun createMethodInvoker(
        controllerInfo: WebControllerProcessFlowInfo,
        controllerClass: Class<*>,
        targetMethod: Method,
        instanceGetter: ControllerInstanceGetter
    ) = WebMethodInvoker(targetMethod, instanceGetter, controllerInfo.controllerContextValueNames).init()


    override fun postLoad() {
        rootRouterMap.apply {
            rootInfoMap.forEach { (name, rootInfo) ->
                put(name, buildRootInfo(rootInfo))
//                put(name, RootRouter(rootInfo.router, ArrayList<ActionInfo<WebActionContext>>().apply {
//                    rootInfo.controllers.forEach { cpfi ->
//                        cpfi.actions.forEach { add(ActionInfo(it.actionClass, it.actionMethod, it.creator())) }
//                    }
//                }))
            }
        }
    }


}