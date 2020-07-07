package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.Yu.controller.NewActionContext
import com.IceCreamQAQ.YuWeb.controller.render.Render
import com.alibaba.fastjson.JSON
import java.lang.reflect.InvocationTargetException

class WebActionContext(override var path: Array<String>, val request: H.Request, val response: H.Response) : NewActionContext {

    var saves = HashMap<String, Any>()
    var success = false

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

    override fun get(name: String): Any? = saves[name] ?: request.para?.get(name)?.get(0) ?: request.session[name]
    override fun set(name: String, obj: Any) {
        saves[name] = obj
    }

    override fun onError(e: Throwable): Throwable? = when (e) {
        is InvocationTargetException -> onError(e.cause!!)
        is Render -> {
            render = e
            null
        }
        else -> e
    }

    override fun onSuccess(result: Any?): Any? {
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
        response.body = JSON.toJSONString(obj)
    }


//    fun getResponse(): H.Response {
//        return this.result as H.Response
//    }
//
//    fun setResponse(response: H.Response) {
//        this.result = response
//    }
//
//    override fun <T : Any?> injectObj(clazz: Class<T>, name: String): T {
//        var re:Any? = null
//        if (clazz == String::class.java)
//            if (request.para != null) {
//                re = request.para!![name]!![0]
//            }
//
//        if (clazz == Array<String>::class.java)
//            re = request.para!![name]!!
//
//        if (clazz == ActionContextBase::class.java || clazz == WebActionContext::class.java)
//            re = this
//
//        if (clazz == H.Cookie::class.java)
//            if (request.cookies != null) {
//                re = request.cookies!![name]
//            }
//
//        if (re == null)re = JSON.parseObject(request.body,clazz)
//
//        return re as T
//
//    }
//
//    override fun <T : Any> injectPathVar(clazz: Class<T>, key: Int, type: PathVar.Type): T? {
//        val para: Any?
//
//        val texts = path
//
//        when (type) {
//            PathVar.Type.string -> para = texts[key]
//            PathVar.Type.flag -> {
//                val text = texts[key]
//                para = text.contains("true") || text.contains("enable")
//            }
//            PathVar.Type.number -> para = texts[key].toInt()
//            else -> para = null
//        }
//
//        return para as T?
//    }
//


}