package smartweb.controller

import rain.controller.ProcessInvoker
import rain.controller.simple.SimpleActionInvoker
import smartweb.temple.Temple
import java.lang.reflect.Method

class WebActionInvoker(
    override val actionClass: Class<*>,
    override val actionMethod: Method,
    val temple: Temple?,
    action: ProcessInvoker<WebActionContext>,
    beforeProcesses: Array<ProcessInvoker<WebActionContext>>,
    afterProcesses: Array<ProcessInvoker<WebActionContext>>,
    catchProcesses: Array<ProcessInvoker<WebActionContext>>
) : SimpleActionInvoker<WebActionContext>(
    actionClass,
    actionMethod,
    action,
    beforeProcesses,
    afterProcesses,
    catchProcesses
) {

    override suspend fun checkChannel(context: WebActionContext) = true

}