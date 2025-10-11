package smartweb.controller

import rain.controller.ControllerProcessFlowInfo

class WebControllerProcessFlowInfo(
    val controllerContextValueNames: List<String>,
    controllerClass: Class<*>,
    controllerInstance: Any,
    controllerChannels: List<String>,
    controllerRouter: WebRouter
) : ControllerProcessFlowInfo<WebActionContext, WebRouter, WebActionInvoker>(
    controllerClass,
    controllerInstance,
    controllerChannels,
    controllerRouter
)