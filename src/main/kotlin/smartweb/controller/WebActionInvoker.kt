package smartweb.controller

import rain.controller.ProcessInvoker
import rain.controller.simple.SimpleActionInvoker
import smartweb.temple.Temple

class WebActionInvoker(
    val allowMethods: List<smartweb.http.HttpMethod>,
    val temple: Temple?,
    action: ProcessInvoker<WebActionContext>,
    beforeProcesses: Array<ProcessInvoker<WebActionContext>>,
    aftersProcesses: Array<ProcessInvoker<WebActionContext>>,
    catchsProcesses: Array<ProcessInvoker<WebActionContext>>
) : SimpleActionInvoker<WebActionContext>(action, beforeProcesses, aftersProcesses, catchsProcesses) {

    override suspend fun checkChannel(context: WebActionContext) = context.requestMethodEnum in allowMethods

}