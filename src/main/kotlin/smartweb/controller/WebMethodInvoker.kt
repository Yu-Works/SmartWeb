package smartweb.controller

import com.alibaba.fastjson2.JSON
import smartweb.annotation.*
import com.alibaba.fastjson2.JSONArray
import rain.api.permission.IUser
import rain.controller.ActionContext
import rain.controller.ControllerInstanceGetter
import rain.controller.simple.SimpleKJReflectMethodInvoker
import rain.controller.simple.SimpleKJReflectMethodInvoker.MethodParam.Companion.annotation
import rain.controller.simple.SimpleKJReflectMethodInvoker.MethodParam.Companion.hasAnnotation
import rain.function.toLowerCaseFirstOne
import smartweb.http.*
import java.lang.reflect.Method

open class WebMethodInvoker(
    method: Method,
    instance: ControllerInstanceGetter,
    private val contextValueKeys: List<String>
) : SimpleKJReflectMethodInvoker<WebActionContext, WebActionContext.() -> Any?>(method, instance) {

    companion object {
        val requestBodyParamName = arrayOf("request", "requestBody", "body")
    }

    open fun WebActionContext.readParam(name: String, type: Class<*>): Any? =
        saves[name]?.let {
            if (type.isInstance(it)) return it
            if (it is String) it.stringAsSimple(type)
            else null
        } ?: params.getObject(name, type)

    open fun WebActionContext.readParamArray(name: String): JSONArray? = params.getJSONArray(name)

    open fun WebActionContext.readBody(type: Class<*>): Any? = params.toJavaObject(type)
    open fun WebActionContext.readBodyArray(): JSONArray? = req.bodyArray


    override fun initParam(method: Method, params: Array<MethodParam<WebActionContext.() -> Any?>>) {

        params.forEach {

            fun valueGetter(body: WebActionContext.() -> Any?) {
                it.attachment = body
                throw RuntimeException()
            }

            if (it.relType.realClass == List::class.java && it.relType.generics!![0].realClass == smartweb.http.UploadFile::class.java) {
                valueGetter { req.uploadFiles?.get(it.name) }
            }

            kotlin.runCatching {
                when (it.type) {
                    ActionContext::class.java, WebActionContext::class.java -> valueGetter { this }
                    Request::class.java -> valueGetter { req }
                    Response::class.java -> valueGetter { resp }
                    Session::class.java -> valueGetter { this.req.session }
                    Cookie::class.java -> valueGetter { this.req.cookie(it.name) }
                    UploadFile::class.java -> valueGetter {
                        req.uploadFiles?.get(it.name)?.let { files ->
                            if (files.size > 1)
                                error("遇到多个文件上传参数 ${it.name}，但参数只接受单个上传文件！")
                            else files[0]
                        }
                    }

                    Exception::class.java -> valueGetter { this.runtimeError }

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

                        if (it.hasAnnotation<ContextValue>() || it.name in contextValueKeys) {
                            if (it.type == ReferenceValue::class.java)
                                valueGetter { ReferenceValue(this[it.name]) { v -> this[it.name] = v } }
                            else valueGetter { this[it.name] }
                        }

                        if (IUser::class.java.isAssignableFrom(it.type))
                            valueGetter { this.user }


                        val isSimple = it.relType.realClass.isSimpleClass()

                        val isBody =
                            !isSimple && !it.hasAnnotation<RequestParam>() && (it.hasAnnotation<RequestBody>() || it.name in requestBodyParamName || it.name == it.type.simpleName.toLowerCaseFirstOne() || method.parameters.size == 1)


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

    open fun Class<*>.isSimpleClass(): Boolean {
        return when (this) {
            Int::class.javaPrimitiveType,
            Int::class.javaObjectType,
            Long::class.javaPrimitiveType,
            Long::class.javaObjectType,
            Float::class.javaPrimitiveType,
            Float::class.javaObjectType,
            Double::class.javaPrimitiveType,
            Double::class.javaObjectType,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaObjectType,
            Char::class.javaPrimitiveType,
            Char::class.javaObjectType,
            Byte::class.javaPrimitiveType,
            Byte::class.javaObjectType,
            Short::class.javaPrimitiveType,
            Short::class.javaObjectType -> true

            else -> false
        }
    }


    override fun getParam(param: MethodParam<WebActionContext.() -> Any?>, context: WebActionContext): Any? =
        param.attachment?.let { context.it() }

}