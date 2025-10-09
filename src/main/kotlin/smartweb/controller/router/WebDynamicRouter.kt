package smartweb.controller.router

import rain.controller.dss.router.DynamicRouter
import rain.controller.dss.router.RouterMatcher
import smartweb.controller.ActionRequestMethodMapping
import smartweb.controller.WebActionContext

class WebDynamicRouter(
    matcher: RouterMatcher<WebActionContext>,
    router: WebStaticRouter,
) : DynamicRouter<WebActionContext, ActionRequestMethodMapping, WebStaticRouter>(matcher, router), WebRouter {
    override val pathVars: Array<String>
        get() = router.pathVars
}