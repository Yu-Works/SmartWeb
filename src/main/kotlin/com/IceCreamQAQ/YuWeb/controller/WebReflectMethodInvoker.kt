package com.IceCreamQAQ.YuWeb.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.controller.ActionContext
import com.IceCreamQAQ.Yu.controller.MethodInvoker
import com.IceCreamQAQ.YuWeb.H
import com.IceCreamQAQ.YuWeb.WebActionContext
import com.alibaba.fastjson.util.TypeUtils
import java.lang.reflect.Method
import java.util.stream.Collectors
import javax.inject.Named
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.kotlinFunction

class WebReflectMethodInvoker(private val method: Method, val instance: Any, level: Int? = null) : MethodInvoker {

    data class MethodPara(
            val clazz: Class<*>,
            val type: Int,
            val data: Any,
            val isArray: Boolean,
            val isSimple: Boolean,
            val cts: (Array<String>.() -> Any?)? = null,
    )

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
        val needMatch = (actionPaths?.size ?: 0 > 1) && isAction
        val l = if (needMatch) level!! - actionPaths!!.size - 1 else 0

        for ((i, para) in paras.withIndex()) {
//                val para = paras[i]!!

            val name = para.getAnnotation(Named::class.java)!!.value

//            if (para.type == H.Cookie::class.java){
//
//            }

            mps[i] = when (para.type) {
                H.Cookie::class.java -> MethodPara(para.type, 1, name, false, false)
                H.Request::class.java -> MethodPara(para.type, 10, name, false, false)
                H.Response::class.java -> MethodPara(para.type, 11, name, false, false)
                H.Session::class.java -> MethodPara(para.type, 12, name, false, false)
                ActionContext::class.java,
                WebActionContext::class.java -> MethodPara(para.type, 15, name, false, false)
                else -> {
                    val p = para.type.isSimpleClass()
                    MethodPara(para.type, 0, name, para.type.isArray, p.first, p.second)
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
            }
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

    operator fun WebActionContext.get(name: String, mp: MethodPara): Any? {
        val clazz = mp.clazz
        saves[name]?.let { if (clazz.isInstance(it)) return it }
        request.session[name]?.let { if (clazz.isInstance(it)) return it }
        val rp = request.para
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
            return TypeUtils.castToJavaBean(q, clazz)
        }
        return if (!mp.isSimple) rp.toJavaObject(clazz) else null
    }

}