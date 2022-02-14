package com.IceCreamQAQ.YuWeb.controller

import com.IceCreamQAQ.Yu.controller.ActionContext
import com.IceCreamQAQ.Yu.controller.DefaultActionInvoker
import com.IceCreamQAQ.Yu.toLowerCaseFirstOne
import com.IceCreamQAQ.YuWeb.WebActionContext
import com.IceCreamQAQ.YuWeb.temple.Temple
import com.IceCreamQAQ.YuWeb.validation.ValidatorFactory
import java.lang.reflect.Method


class WebActionInvoker(
    level: Int, method: Method,
    instance: Any,
    val factory: ValidatorFactory,
    val temple: Temple?
) : DefaultActionInvoker(level, method, instance) {
//    override val invoker: MethodInvoker = WebReflectMethodInvoker(method, instance, null, factory)

    override fun createMethodInvoker(method: Method, instance: Any) =
        WebReflectMethodInvoker(method, instance, level, factory)

    override suspend fun invoke(path: String, context: ActionContext): Boolean {
//        if (super.invoke(path, context)) return true
        try {
            context as WebActionContext
            context.invoker = this
            for (before in befores) {
                val o = before.invoke(context)
                if (o != null) context[o::class.java.simpleName.toLowerCaseFirstOne()] = o
            }
            val result = invoker.invoke(context)
            context.onSuccess(result)
            for (after in afters) {
                val o = after.invoke(context)
                if (o != null) context[o::class.java.simpleName.toLowerCaseFirstOne()] = o
            }
        } catch (e: Exception) {
            val er = context.onError(e) ?: return true
            context["exception"] = er
            try {
                for (catch in catchs) {
                    val o = catch.invoke(context, er)
                    if (o != null) context[o::class.java.simpleName.toLowerCaseFirstOne()] = o
                }

            } catch (ee: Exception) {
                throw context.onError(ee) ?: return true
            }
            throw er
        }
        return true
    }
}