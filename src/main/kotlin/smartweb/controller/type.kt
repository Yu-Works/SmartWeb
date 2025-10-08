package smartweb.controller

import rain.controller.RootRouter
import rain.controller.RootRouterProcessFlowInfo
import rain.controller.dss.router.DssRouter

typealias WebRouter = DssRouter<WebActionContext, ActionRequestMethodMapping>
typealias WebRootRouter = RootRouter<WebActionContext, WebRouter, WebActionInvoker>
typealias WebRootInfo = RootRouterProcessFlowInfo<WebActionContext, WebRouter, WebActionInvoker>