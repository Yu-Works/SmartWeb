package com.IceCreamQAQ.SmartWeb.controller

import com.IceCreamQAQ.SmartWeb.annotation.*
import com.IceCreamQAQ.SmartWeb.http.Cookie
import com.IceCreamQAQ.SmartWeb.http.Request
import com.IceCreamQAQ.SmartWeb.http.Response
import com.IceCreamQAQ.SmartWeb.http.Session
import com.IceCreamQAQ.Yu.controller.ActionContext
import com.IceCreamQAQ.Yu.controller.ControllerInstanceGetter
import com.IceCreamQAQ.Yu.controller.simple.SimpleKJReflectMethodInvoker
import com.IceCreamQAQ.Yu.controller.simple.SimpleKJReflectMethodInvoker.MethodParam.Companion.annotation
import com.IceCreamQAQ.Yu.controller.simple.SimpleKJReflectMethodInvoker.MethodParam.Companion.hasAnnotation
import com.IceCreamQAQ.Yu.toLowerCaseFirstOne
import com.alibaba.fastjson2.JSONArray
import java.lang.reflect.Method

open class WebMethodInvoker(
    method: Method,
    instance: ControllerInstanceGetter
) : SimpleKJReflectMethodInvoker<WebActionContext, WebActionContext.() -> Any?>(method, instance) {

    companion object {
        val requestBodyParamName = arrayOf("request", "requestBody", "body")
    }

    fun WebActionContext.readParam(name: String, type: Class<*>): Any? =
        saves[name]?.let {
            if (type.isInstance(it)) return it
            if (it is String) it.stringAsSimple(type)
            else null
        } ?: params.getObject(name, type)

    fun WebActionContext.readParamArray(name: String): JSONArray? = params.getJSONArray(name)

    fun WebActionContext.readBody(type: Class<*>): Any? = params.toJavaObject(type)
    fun WebActionContext.readBodyArray(): JSONArray? = req.bodyArray


    override fun initParam(method: Method, params: Array<MethodParam<WebActionContext.() -> Any?>>) {
        var effectiveParam = 0

        params.forEach {

            fun valueGetter(body: WebActionContext.() -> Any?) {
                it.attachment = body
                throw RuntimeException()
            }

            kotlin.runCatching {
                when (it.type) {
                    ActionContext::class.java, WebActionContext::class.java -> valueGetter { this }
                    Request::class.java -> valueGetter { req }
                    Response::class.java -> valueGetter { resp }
                    Session::class.java -> valueGetter { this.req.session }
                    Cookie::class.java -> valueGetter { this.req.cookie(it.name) }
                    else -> {
                        it.annotation<SessionValue> {
                            if (it.type == ReferenceValue::class.java)
                                valueGetter {
                                    ReferenceValue(this.req.session[it.name]) { v ->
                                        this.req.session[it.name] = v
                                    }
                                }
                            else valueGetter { this.req.session[it.name] }
                        }

                        it.annotation<CookieValue> {
                            if (it.type == ReferenceValue::class.java) {
                                val type = it.relType.generics!![0].realClass

                                val valueOf = if (type == String::class.java) {
                                    { v -> v }
                                } else if (it.type.isSimpleClass()) it.type.simpleClassValueOf()
                                else error("遇到 CookieValue 的解析到类型 ${type.name}！")

                                valueGetter {
                                    ReferenceValue(this.req.cookie(it.name)?.value?.let(valueOf)) { v ->
                                        this.resp.addCookie(it.name, v?.toString() ?: error("CookieValue 不可为空！"))
                                    }
                                }
                            } else {
                                val type = it.type

                                val valueOf = if (type == String::class.java) {
                                    { v -> v }
                                } else it.type.simpleClassValueOf()

                                valueGetter { this.req.cookie(it.name)?.value?.let(valueOf) }
                            }
                        }

                        it.annotation<ContextValue> {
                            if (it.type == ReferenceValue::class.java)
                                valueGetter { ReferenceValue(this[it.name]) { v -> this[it.name] = v } }
                            else valueGetter { this[it.name] }
                        }


                        val isSimple = it.type.isSimpleClass()

                        val isBody =
                            !isSimple && !it.hasAnnotation<RequestParam>() && (it.hasAnnotation<RequestBody>() || it.name in requestBodyParamName || it.name == it.type.name.toLowerCaseFirstOne())


                        val isArray = it.type.isArray
                        val isList = isArray || List::class.java.isAssignableFrom(it.type)

                        val type =
                            if (isArray) it.type.componentType
                            else if (isList) it.relType.generics!![0].realClass
                            else it.type

                        val reader: WebActionContext.() -> Any? =
                            if (isBody) {
                                if (isList) {
                                    { readBodyArray()?.toList(type) }
                                } else {
                                    { readBody(type) }
                                }
                            } else {
                                if (isList) {
                                    if (isArray) {
                                        { readParamArray(it.name)?.toArray(type) }
                                    } else {
                                        { readParamArray(it.name)?.toList(type) }
                                    }
                                } else {
                                    { readParam(it.name, type) }
                                }
                            }
                        it.attachment = reader
                    }
                }
            }.onFailure { err ->
                if (err is RuntimeException) return@forEach
                throw err
            }
        }
    }

    fun Class<*>.isSimpleClass(): Boolean {
        return when (this) {
            Int::class.java, Long::class.java, Float::class.java, Double::class.java, Boolean::class.java, Char::class.java, Byte::class.java, Short::class.java -> true
            else -> false
        }
    }


    override fun getParam(param: MethodParam<WebActionContext.() -> Any?>, context: WebActionContext): Any? =
        param.attachment?.let { context.it() }

}