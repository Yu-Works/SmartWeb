package com.IceCreamQAQ.SmartWeb.controller

import com.IceCreamQAQ.SmartWeb.http.HttpMethod
import com.IceCreamQAQ.Yu.controller.ProcessInvoker
import com.IceCreamQAQ.Yu.controller.dss.DssActionInvoker
import com.IceCreamQAQ.Yu.controller.dss.router.RouterMatcher
import com.IceCreamQAQ.SmartWeb.temple.Temple

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