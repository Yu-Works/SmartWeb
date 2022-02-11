package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.Yu.controller.ActionContext
import com.IceCreamQAQ.Yu.web.ActionResult
import com.IceCreamQAQ.YuWeb.controller.WebActionInvoker
import com.IceCreamQAQ.YuWeb.controller.render.Render
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.util.TypeUtils
import java.lang.reflect.InvocationTargetException

class WebActionContext(
    override var path: Array<String>,
    val request: H.Request,
    val response: H.Response
) : ActionContext {

    var saves = HashMap<String, Any>()
    var success = false

    lateinit var invoker: WebActionInvoker

    var render: Render? = null

    init {
        saves["context"] = this
        saves["ActionContext"] = this
        saves["webActionContext"] = this

        saves["req"] = request
        saves["request"] = request

        saves["resp"] = response
        saves["response"] = response
    }

    fun getCookie(name: String): H.Cookie? = request.cookies?.get(name)

    override fun get(name: String): Any? = saves[name] ?: request.para[name] ?: request.session[name]
    override fun set(name: String, obj: Any) {
        saves[name] = obj
    }

    override suspend fun onError(e: Throwable): Throwable? = when (e) {
        is InvocationTargetException -> onError(e.cause!!)
        is Render -> {
            render = e
            null
        }
        is ActionResult -> {
            buildResult(e.result)
            null
        }
        else -> e
    }

    override suspend fun onSuccess(result: Any?): Any? {
        if (result == null) return null
        when (result) {
            is String -> buildResult(result)
            else -> buildResult(result)
        }
        return null
    }

    private fun buildResult(text: String) {
        when {
            text.startsWith("{") || text.startsWith("[") -> response.contentType = "application/json"
            text.startsWith("<?xml") -> response.contentType = "application/xml"
            text.startsWith("<") -> response.contentType = "text/html"
            else -> response.contentType = "text/plain"
        }

        response.body = text
    }

    private fun buildResult(obj: Any?) {
        response.contentType = "application/json"
        response.body = WebServer.jsonEncoder(this, JSON.toJSONString(obj))
    }

}