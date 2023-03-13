package com.IceCreamQAQ.SmartWeb.controller

import com.IceCreamQAQ.Yu.controller.RootRouter
import com.IceCreamQAQ.Yu.controller.RootRouterProcessFlowInfo
import com.IceCreamQAQ.Yu.controller.dss.router.DssRouter

typealias WebRouter = DssRouter<WebActionContext>
typealias WebRootRouter = RootRouter<WebActionContext, WebRouter>
typealias WebRootInfo = RootRouterProcessFlowInfo<WebActionContext, DssRouter<WebActionContext>>