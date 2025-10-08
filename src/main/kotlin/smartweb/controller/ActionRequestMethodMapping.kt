package smartweb.controller

import rain.controller.ActionInvoker
import smartweb.http.HttpMethod
import java.lang.reflect.Method

class ActionRequestMethodMapping : ActionInvoker<WebActionContext> {

    var getAction: WebActionInvoker? = null
    var postAction: WebActionInvoker? = null
    var putAction: WebActionInvoker? = null
    var deleteAction: WebActionInvoker? = null
    var patchAction: WebActionInvoker? = null

    val actionMap: MutableMap<String, WebActionInvoker>
        get() {
            if (_actionMap == null) _actionMap = HashMap()
            return _actionMap!!
        }
   private var _actionMap: MutableMap<String, WebActionInvoker>? = null

    override val actionClass: Class<*>?
        get() = null
    override val actionMethod: Method?
        get() = null

    override suspend fun invoke(context: WebActionContext): Boolean =
        run {
            when(context.requestMethodEnum){
                HttpMethod.GET -> getAction
                HttpMethod.POST -> postAction
                HttpMethod.PUT -> putAction
                HttpMethod.DELETE -> deleteAction
                HttpMethod.PATCH -> patchAction
                else -> null
            } ?: _actionMap?.get(context.requestMethodString)
        }?.invoke(context) ?: false
}