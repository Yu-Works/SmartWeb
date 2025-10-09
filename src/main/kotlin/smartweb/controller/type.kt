package smartweb.controller

import rain.controller.RootRouter
import rain.controller.RootRouterProcessFlowInfo
import rain.controller.dss.router.DssRouter
import smartweb.controller.router.WebStaticRouter

//typealias WebRouter = DssRouter<WebActionContext, ActionRequestMethodMapping>
typealias WebRouter = WebStaticRouter
typealias WebRootRouter = RootRouter<WebActionContext, WebRouter, WebActionInvoker>
typealias WebRootInfo = RootRouterProcessFlowInfo<WebActionContext, WebRouter, WebActionInvoker>