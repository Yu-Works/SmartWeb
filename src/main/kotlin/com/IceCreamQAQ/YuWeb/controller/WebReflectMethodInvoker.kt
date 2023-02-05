package com.IceCreamQAQ.YuWeb.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Default
import com.IceCreamQAQ.Yu.controller.ActionContext
import com.IceCreamQAQ.Yu.controller.MethodInvoker
import com.IceCreamQAQ.Yu.toObject
import com.IceCreamQAQ.Yu.validation.ValidateData
import com.IceCreamQAQ.Yu.validation.ValidateFailException
import com.IceCreamQAQ.YuWeb.H
import com.IceCreamQAQ.YuWeb.WebActionContext
import com.IceCreamQAQ.YuWeb.annotation.Output
import com.IceCreamQAQ.YuWeb.annotation.RequestBody
import com.IceCreamQAQ.YuWeb.annotation.RequestParameter
import com.IceCreamQAQ.YuWeb.toParaName
import com.IceCreamQAQ.YuWeb.validation.*
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.util.TypeUtils
import java.lang.reflect.Method
import javax.inject.Named
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.kotlinFunction

class WebReflectMethodInvoker(
    private val method: Method,
    val instance: Any,
    level: Int? = null,
//    factory: ValidatorFactory
) : MethodInvoker {

    data class MethodPara(
        val clazz: Class<*>,
        val name: String,
        val type: Int,
        val data: Any,
        val isArray: Boolean,
        val isSimple: Boolean,
        val isSaved: Boolean,
        val default: String?,
        var isBody: Boolean,
        var isPara: Boolean,
        val cts: (Array<String>.() -> Any?)? = null,
        val vds: Array<ValidateData>?
    )

    val className = instance::class.java.name
    val methodName = method.name

    private var returnFlag: Boolean = false
    private lateinit var mps: Array<MethodPara>
    lateinit var kFun: KFunction<*>
    var isSuspend = false
//    val body: Boolean

    init {
        returnFlag = (method.returnType?.name ?: "void") != "void"

        val paras = method.parameters!!
        val mps = arrayOfNulls<MethodPara>(paras.size)

        val action = method.getAnnotation(Action::class.java)
        val isAction = action != null && level != null
        val actionPaths = action?.value?.split(" ", "/")
        val needMatch = ((actionPaths?.size ?: 0) > 1) && isAction
        val l = if (needMatch) level!! - actionPaths!!.size - 1 else 0

        for ((i, para) in paras.withIndex()) {
//                val para = paras[i]!!

            val name = para.getAnnotation(Named::class.java)!!.value

//            if (para.type == H.Cookie::class.java){
//
//            }
            val vds = arrayListOf<ValidateData>()
//            para.type.getAnnotation(Valid::class.java)?.let { vds.add(ValidateData(it, factory[para.type])) }
//
//            for (annotation in para.annotations) {
//                val vb = annotation::class.java.interfaces[0].getAnnotation(ValidateBy::class.java) ?: continue
//                vds.add(ValidateData(annotation, factory[vb.value]))
//            }

            fun buildMP(
                type: Int,
                data: Any = name,
                isArray: Boolean = false,
                isSimple: Boolean = false,
                cts: (Array<String>.() -> Any?)? = null
            ) = MethodPara(
                para.type,
                name,
                type,
                data,
                isArray,
                isSimple,
                para.getAnnotation(Output::class.java) != null,
                para.getAnnotation(Default::class.java)?.value,
                para.type.getAnnotation(RequestBody::class.java) != null,
                para.type.getAnnotation(RequestParameter::class.java) != null,
                cts,
                if (vds.size == 0) null else vds.toTypedArray()
            )

            mps[i] = when (para.type) {
                H.Cookie::class.java -> buildMP(1)
                H.Request::class.java -> buildMP(10)
                H.Response::class.java -> buildMP(11)
                H.Session::class.java -> buildMP(12)
                ActionContext::class.java,
                WebActionContext::class.java -> buildMP(15)
                else -> {
                    val p = para.type.isSimpleClass()
                    buildMP(0, name.toParaName(), para.type.isArray, p.first, p.second)
                }
            }
        }

        method.kotlinFunction?.let {
            kFun = it
            if (it.isSuspend) isSuspend = true
        }

        this.mps = mps as Array<MethodPara>

//        body = if (mps.size == 1)
//            when (mps[0].clazz) {
//                Char::class.java, Char::class.javaObjectType -> false
//                Boolean::class.java, Boolean::class.javaObjectType -> false
//                Short::class.java, Short::class.javaObjectType -> false
//                Int::class.java, Int::class.javaObjectType -> false
//                Float::class.java, Float::class.javaObjectType -> false
//                Long::class.java, Long::class.javaObjectType -> false
//                Double::class.java, Double::class.javaObjectType -> false
//                String::class.java -> false
//                else -> true
//            }
//        else false
    }

//    fun checkType(clazz: Class<*>) = when(clazz.name){
//        Int::class.java -> java.
//    }

    private inline fun <T> Array<String>.cpt(array: Array<T>, ptc: (String) -> T): Any? {
        for ((i, v) in this.withIndex()) {
            array[i] = ptc(v)
        }
        return array
    }

    private inline fun Array<String>.cpt(array: CharArray, ptc: (String) -> Char): Any? {
        for ((i, v) in this.withIndex()) {
            array[i] = ptc(v)
        }
        return array
    }

    private inline fun Array<String>.cpt(array: BooleanArray, ptc: (String) -> Boolean): Any? {
        for ((i, v) in this.withIndex()) {
            array[i] = ptc(v)
        }
        return array
    }

    private inline fun Array<String>.cpt(array: ShortArray, ptc: (String) -> Short): Any? {
        for ((i, v) in this.withIndex()) {
            array[i] = ptc(v)
        }
        return array
    }

    private inline fun Array<String>.cpt(array: IntArray, ptc: (String) -> Int): Any? {
        for ((i, v) in this.withIndex()) {
            array[i] = ptc(v)
        }
        return array
    }

    private inline fun Array<String>.cpt(array: FloatArray, ptc: (String) -> Float): Any? {
        for ((i, v) in this.withIndex()) {
            array[i] = ptc(v)
        }
        return array
    }

    private inline fun Array<String>.cpt(array: LongArray, ptc: (String) -> Long): Any? {
        for ((i, v) in this.withIndex()) {
            array[i] = ptc(v)
        }
        return array
    }

    private inline fun Array<String>.cpt(array: DoubleArray, ptc: (String) -> Double): Any? {
        for ((i, v) in this.withIndex()) {
            array[i] = ptc(v)
        }
        return array
    }
//    private inline fun Array<String>.cpt(array: , ptc: (String) -> ): Any? {
//        for ((i, v) in this.withIndex()) {
//            array[i] = ptc(v)
//        }
//        return array
//    }


    fun Class<*>.isSimpleClass(): Pair<Boolean, Array<String>.() -> Any?> = when (this.componentType) {
        Char::class.java -> true to { cpt(CharArray(this.size)) { it[0] } }
        Boolean::class.java -> true to { cpt(BooleanArray(this.size)) { it.toBoolean() } }
        Short::class.java -> true to { cpt(ShortArray(this.size)) { it.toShort() } }
        Int::class.java -> true to { cpt(IntArray(this.size)) { it.toInt() } }
        Float::class.java -> true to { cpt(FloatArray(this.size)) { it.toFloat() } }
        Long::class.java -> true to { cpt(LongArray(this.size)) { it.toLong() } }
        Double::class.java -> true to { cpt(DoubleArray(this.size)) { it.toDouble() } }

        Char::class.javaObjectType -> true to { cpt(arrayOfNulls(this.size)) { it[0] } }
        Boolean::class.javaObjectType -> true to { cpt(arrayOfNulls(this.size)) { it.toBoolean() } }
        Short::class.javaObjectType -> true to { cpt(arrayOfNulls(this.size)) { it.toShort() } }
        Int::class.javaObjectType -> true to { cpt(arrayOfNulls(this.size)) { it.toInt() } }
        Float::class.javaObjectType -> true to { cpt(arrayOfNulls(this.size)) { it.toFloat() } }
        Long::class.javaObjectType -> true to { cpt(arrayOfNulls(this.size)) { it.toLong() } }
        Double::class.javaObjectType -> true to { cpt(arrayOfNulls(this.size)) { it.toDouble() } }
        String::class.java -> true to { cpt(arrayOfNulls(this.size)) { it } }
        else -> false to { }
    }

    override suspend fun invoke(context: ActionContext): Any? {
        if (context !is WebActionContext) return null

        val paras = if (isSuspend) getParas(mps.size - 1, context) else getParas(mps.size, context)

        return if (isSuspend) kFun.callSuspend(instance, *paras)
        else if (mps.isEmpty()) method.invoke(instance)
        else method.invoke(instance, *paras)
    }

    fun getParas(len: Int, context: WebActionContext): Array<Any?> {
//        if (body) return arrayOf(context.getBody(mps[0].data.toString(), mps[0].clazz))
        val paras = arrayOfNulls<Any>(len)
        for (i in 0 until len) {
            val mp = mps[i]
//            val name
//            paras[i] = context[mp.data.toString(), mp.clazz]
            val p = when (mp.type) {
                0 -> context[mp.data.toString(), mp]
                1 -> context.getCookie(mp.data.toString())
                10 -> context.request
                11 -> context.response
                12 -> context.request.session
                15 -> context
                else -> context[mp.data.toString(), mp]
            } ?: mp.default?.let { str2Type(it, mp.clazz) }
            mp.vds?.let {
                for (vd in it) {
                    vd.validator.validate(vd.annotation, p)
                        ?.run { throw ValidateFailException(methodName, mp.name, this) }
                }
            }
            p?.let { if (mp.isSaved) context[mp.name] = it }
            paras[i] = p
        }
        return paras
    }

//    fun WebActionContext.getBody(name: String, clazz: Class<*>): Any? {
//        saves[name]?.let { if (clazz.isInstance(it)) return it }
//        request.session[name]?.let { if (clazz.isInstance(it)) return it }
//        val rp = request.para
//        if (rp.containsKey(name)) {
//            val q = rp[name]!!
//            if (clazz.isArray) if (q::class.java.isArray) return q else arrayOf(q)
//            else if (q::class.java.isArray) return (q as Array<*>).let { if (it.isNotEmpty()) it[0] else null }
//            return TypeUtils.castToJavaBean(q, clazz)
//        }
//        return rp.toJavaObject(clazz)
//    }

    companion object {
        val reqParaName = arrayOf("req", "request", "paras", "parameters", "body", "reqbody", "requestbody")
    }

    fun str2Type(data: String, clazz: Class<*>) =
        when (clazz) {
            String::class.java -> data
            Boolean::class.java, Boolean::class.javaObjectType -> data.toBoolean()
            Byte::class.java, Byte::class.javaObjectType -> data.toByte()
            Short::class.java, Short::class.javaObjectType -> data.toShort()
            Int::class.java, Int::class.javaObjectType -> data.toInt()
            Long::class.java, Long::class.javaObjectType -> data.toLong()
            Char::class.java, Char::class.javaObjectType -> data[0]
            Float::class.java, Float::class.javaObjectType -> data.toFloat()
            Double::class.java, Double::class.javaObjectType -> data.toDouble()
            else -> {
                if (clazz.isArray) {
                    if (!data.startsWith("[") || !data.endsWith("]")) error("目标类型为数组，但值并不以 '[' 开头，或不以 ']' 结尾。")
                    JSON.parseArray(data, clazz).toTypedArray()
                } else data.toObject(clazz)
            }
        }

    operator fun WebActionContext.get(name: String, mp: MethodPara): Any? {
        val clazz = mp.clazz
        saves[name]?.let {
            if (clazz.isInstance(it)) return it
            if (it is String) {
                when (clazz) {
                    Boolean::class.java, Boolean::class.javaObjectType -> return it.toBoolean()
                    Byte::class.java, Byte::class.javaObjectType -> return it.toByte()
                    Short::class.java, Short::class.javaObjectType -> return it.toShort()
                    Int::class.java, Int::class.javaObjectType -> return it.toInt()
                    Long::class.java, Long::class.javaObjectType -> return it.toLong()
                    Char::class.java, Char::class.javaObjectType -> return it[0]
                    Float::class.java, Float::class.javaObjectType -> return it.toFloat()
                    Double::class.java, Double::class.javaObjectType -> return it.toDouble()
                }
            }
        }
        request.session[name]?.let { if (clazz.isInstance(it)) return it }
        val rp = paras
        if (mp.isBody) return rp.toJSONString()
        if (rp.containsKey(name)) {
            var q = rp[name]!!
            if (mp.isArray) {
                if (q::class.java.isArray)
                    if (clazz.isAssignableFrom(q::class.java)) return q
                    else q = (q as Array<*>)[0]!!
                if (q is String && mp.isSimple) return mp.cts!!(q.split(",").toTypedArray())
                else
                    if (clazz.isAssignableFrom(q::class.java)) return arrayOf(q)
            } else
                if (q::class.java.isArray)
                    if (clazz.isAssignableFrom(q::class.java)) return (q as Array<*>).let { if (it.isNotEmpty()) it[0] else null }
            return TypeUtils.cast(q, clazz)
        }
        return if (
            !mp.isPara &&
            !mp.isSimple &&
            rp.size > 0 &&
            (mp.name == mp.clazz.simpleName.toLowerCase() || mp.name in reqParaName)
        ) rp.toJavaObject(clazz) else null
    }

}