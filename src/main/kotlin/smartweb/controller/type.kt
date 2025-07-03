package smartweb.controller

import rain.controller.RootRouter
import rain.controller.RootRouterProcessFlowInfo
import rain.controller.dss.router.DssRouter

typealias WebRouter = DssRouter<WebActionContext, ActionRequestMethodMapping>
typealias WebRootRouter = RootRouter<WebActionContext, WebRouter>
typealias WebRootInfo = RootRouterProcessFlowInfo<WebActionContext, DssRouter<WebActionContext, ActionRequestMethodMapping>>