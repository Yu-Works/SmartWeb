package com.IceCreamQAQ.YuWeb.controller

import com.IceCreamQAQ.Yu.controller.ActionContext
import com.IceCreamQAQ.Yu.controller.NewActionContext
import com.IceCreamQAQ.Yu.controller.router.DefaultActionInvoker
import com.IceCreamQAQ.Yu.controller.router.NewActionInvoker
import com.IceCreamQAQ.Yu.entity.DoNone
import com.IceCreamQAQ.Yu.entity.Result
import com.IceCreamQAQ.YuWeb.WebActionContext
import java.lang.Exception

open class WebActionInvoker(level: Int) : NewActionInvoker(level) {

    override fun invoke(path: String, context: NewActionContext): Boolean {
        if (context !is WebActionContext) return false
//        if (super.invoke(path, context)) return true
        try {
            for (before in befores) {
                val o = before.invoke(context)
                if (o != null) context[toLowerCaseFirstOne(o::class.java.simpleName)] = o
            }
            val result = invoker.invoke(context) ?: return true
            context.response.body = (result.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun toLowerCaseFirstOne(s: String): String {
        return if (Character.isLowerCase(s[0])) s
        else (StringBuilder()).append(Character.toLowerCase(s[0])).append(s.substring(1)).toString();
    }
}