package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.Yu.controller.ActionContext
import com.IceCreamQAQ.YuWeb.controller.WebActionInvoker
import com.IceCreamQAQ.YuWeb.controller.render.Render
import com.alibaba.fastjson.JSONObject
import java.lang.reflect.InvocationTargetException

class WebActionContext(
    override var path: Array<String>,
    val request: H.Request,
    val response: H.Response
) : ActionContext {

    var paras = JSONObject()
    var saves = HashMap<String, Any>()
    var success = false

    var invoker: WebActionInvoker? = null

    var result: Any? = null

    init {
        saves["context"] = this
        saves["ActionContext"] = this
        saves["webActionContext"] = this

        saves["req"] = request
        saves["request"] = request

        saves["resp"] = response
        saves["response"] = response
    }

    fun getCookie(name: String): H.Cookie? = request.cookie(name)

    override fun get(name: String): Any? = saves[name] ?: request.getParameter(name) ?: request.session[name]
    override fun set(name: String, obj: Any) {
        saves[name] = obj
    }

    override suspend fun onError(e: Throwable): Throwable? = when (e) {
        is InvocationTargetException -> onError(e.cause!!)
        is Render -> {
            result = e
            null
        }
        is ActionResult -> {
            result = e.result
            null
        }
        else -> e
    }

    override suspend fun onSuccess(result: Any?): Any? {
        this.result = result
        return null
    }



}