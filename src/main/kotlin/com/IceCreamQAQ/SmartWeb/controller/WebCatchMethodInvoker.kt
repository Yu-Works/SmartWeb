package com.IceCreamQAQ.SmartWeb.controller

import com.IceCreamQAQ.Yu.controller.ControllerInstanceGetter
import java.lang.reflect.Method

class WebCatchMethodInvoker(
    method: Method,
    instance: ControllerInstanceGetter,
    val throwableType: Class<out Throwable>
) : WebMethodInvoker(method, instance) {

    override suspend fun invoke(context: WebActionContext): Any? {
        if (!throwableType.isInstance(context.runtimeError)) return null
        return super.invoke(context)
    }

}