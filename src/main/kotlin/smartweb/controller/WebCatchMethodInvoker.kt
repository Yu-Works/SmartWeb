package smartweb.controller

import rain.controller.ControllerInstanceGetter
import java.lang.reflect.Method

class WebCatchMethodInvoker(
    method: Method,
    instance: ControllerInstanceGetter,
    val throwableType: Class<out Throwable>,
    contextValueKeys: List<String>
) : WebMethodInvoker(method, instance, contextValueKeys) {

    override suspend fun invoke(context: WebActionContext): Any? {
        if (!throwableType.isInstance(context.runtimeError)) return null
        return super.invoke(context)
    }

}