package smartweb.controller.router

import rain.controller.dss.router.DssRouter
import smartweb.controller.ActionRequestMethodMapping
import smartweb.controller.WebActionContext

class WebStaticRouter(
    level: Int,
    override val pathVars: Array<String> = emptyArray()
) : DssRouter<WebActionContext, ActionRequestMethodMapping, WebStaticRouter, WebDynamicRouter>(level), WebRouter