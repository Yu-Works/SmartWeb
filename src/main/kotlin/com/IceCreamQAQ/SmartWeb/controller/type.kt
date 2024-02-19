package com.IceCreamQAQ.SmartWeb.controller

import rain.controller.RootRouter
import rain.controller.RootRouterProcessFlowInfo
import rain.controller.dss.router.DssRouter

typealias WebRouter = DssRouter<WebActionContext>
typealias WebRootRouter = RootRouter<WebActionContext, WebRouter>
typealias WebRootInfo = RootRouterProcessFlowInfo<WebActionContext, DssRouter<WebActionContext>>