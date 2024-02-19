package com.IceCreamQAQ.SmartWeb.controller

import com.IceCreamQAQ.SmartWeb.http.HttpMethod
import com.IceCreamQAQ.SmartWeb.temple.Temple
import rain.controller.ProcessInvoker
import rain.controller.dss.DssActionInvoker
import rain.controller.dss.router.RouterMatcher

class WebActionInvoker(
    val allowMethods: List<HttpMethod>,
    val temple: Temple?,
    level: Int,
    matchers: List<RouterMatcher<WebActionContext>>,
    action: ProcessInvoker<WebActionContext>,
    beforeProcesses: Array<ProcessInvoker<WebActionContext>>,
    aftersProcesses: Array<ProcessInvoker<WebActionContext>>,
    catchsProcesses: Array<ProcessInvoker<WebActionContext>>
) : DssActionInvoker<WebActionContext>(level, matchers, action, beforeProcesses, aftersProcesses, catchsProcesses) {

    override suspend fun invoke(context: WebActionContext): Boolean {
        return super.invoke(context)
    }

    override suspend fun checkChannel(context: WebActionContext) = context.requestMethod in allowMethods

}