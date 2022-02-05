package com.IceCreamQAQ.YuWeb.validation

import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

annotation class Valid

abstract class ValidatorBase<T : Annotation> : Validator {

    private val allowAnnotationClass = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
    private val messageBase = "在进行对象属性值比对时发生错误，声明注解不是验证器所允许的注解类型！验证器：${this::class.java.name}，允许的注解：${allowAnnotationClass.name}，提供的注解：s%。"
    private val nullMessageBase = "在进行对象属性值比对时发生错误，对象值为空！验证器：${this::class.java.name}，允许的注解：${allowAnnotationClass.name}，提供的注解：s%。"
//    abstract

    fun T.buildResult(
            message: String = messageFun(this),
            fullMessage: String = message,
    ) = ValidateResult(message, this@ValidatorBase, fullMessage, this)

    abstract fun messageFun(annotation: T): String

    override fun validate(annotation: Annotation, bean: Any?): ValidateResult? {
        if (!allowAnnotationClass.isInstance(annotation)) return ValidateResult(messageBase.format(annotation::class.java.interfaces[0].name), this, annotation = annotation)
        return doValidate(annotation as T, bean)
    }

    open fun doValidate(annotation: T, bean: Any?): ValidateResult? {
        if (bean == null) return ValidateResult(nullMessageBase.format(annotation::class.java.interfaces[0].name), this, annotation = annotation)
        return doNotNullValidate(annotation, bean)
    }

    open fun doNotNullValidate(annotation: T, bean: Any): ValidateResult? {
        return null
    }
}

annotation class ValidateBy(
        val value: KClass<out Validator>
)

@ValidateBy(NullValidator::class)
annotation class Null(
        val message: String = "",
)

class NullValidator : Validator {
    override fun validate(annotation: Annotation, bean: Any?): ValidateResult? {
        return (annotation as? Null)?.run {
            if (bean != null) ValidateResult(message, this@NullValidator, annotation = this)
            else return null
        }
                ?: ValidateResult("在进行对象属性值比对时发生错误，对象不是验证器所允许的注解类型！验证器：${this::class.java.name}，提供注解：${annotation::class.java.interfaces[0].name}，允许的类型：${Null::class.java.name}。", this, annotation = annotation)
    }
}

@ValidateBy(NotNullValidator::class)
annotation class NotNull(
        val message: String = "",
)

class NotNullValidator : Validator {
    override fun validate(annotation: Annotation, bean: Any?): ValidateResult? {
        return (annotation as? NotNull)?.run {
            if (bean == null) ValidateResult(message, this@NotNullValidator, annotation = this)
            else return null
        }
                ?: ValidateResult("在进行对象属性值比对时发生错误，对象不是验证器所允许的注解类型！验证器：${this::class.java.name}，提供注解：${annotation::class.java.interfaces[0].name}，允许的类型：${NotNull::class.java.name}。", this, annotation = annotation)
    }
}

@ValidateBy(MinValidator::class)
@Target(AnnotationTarget.FIELD,AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
annotation class Min(
        val value: Long,
        val canEqual: Boolean = false,
        val message: String = "所提供数值过小！",
)

class MinValidator : ValidatorBase<Min>() {
//    override val allowAnnotationClass = Min::class.java
//
//    override val messageFun = Min::message

    override fun doNotNullValidate(annotation: Min, bean: Any): ValidateResult? {
        val number = when (bean) {
            is Short -> bean.toLong()
            is Int -> bean.toLong()
            is Long -> bean.toLong()
            is Float -> bean.toLong()
            is Double -> bean.toLong()
            else -> return buildResult("对象并非数值！", annotation)
        }

        return if (annotation.canEqual) if (annotation.value <= number) null else annotation.buildResult()
        else if (annotation.value < number) null else annotation.buildResult()
    }

    override fun messageFun(annotation: Min) = annotation.message


//    override fun doNotNullValidate(annotation: Annotation, bean: Any): ValidateResult? {
//        val number = bean as? Long ?: ValidateResult()
//    }

//    override fun validate(annotation: Annotation, bean: Any?): ValidateResult? {
//        return (annotation as? Min)?.run {
//            if (bean == null) ValidateResult(message, this@MinValidator, annotation = this)
//            else return null
//        }
//                ?: ValidateResult("在进行对象属性值比对时发生错误，对象不是验证器所允许的注解类型！验证器：${this::class.java.name}，提供注解：${annotation::class.java.interfaces[0].name}，允许的类型：${NotNull::class.java.name}。", this, annotation = annotation)
//    }
//    override fun validate(annotation: Annotation, bean: Any?) =
//            (annotation as? Min)?.let {
//                (bean as? Long)?.run {
//                    if (it.canEqual) it.value <= bean
//                    else it.value < bean
//                }
//            } ?: false
}

fun Validator.buildResult(
        message: String,
        annotation: Annotation? = null,
        fullMessage: String = message,
) = ValidateResult(message, this, fullMessage, annotation)

@ValidateBy(MaxValidator::class)
annotation class Max(
        val value: Long,
        val canEqual: Boolean = false,
        val message: String = "所提供数值过大！",
)

class MaxValidator : ValidatorBase<Max>() {

    override fun doNotNullValidate(annotation: Max, bean: Any): ValidateResult? {
        val number = when (bean) {
            is Short -> bean.toLong()
            is Int -> bean.toLong()
            is Long -> bean.toLong()
            is Float -> bean.toLong()
            is Double -> bean.toLong()
            else -> return buildResult("对象并非数值！", annotation)
        }

        return if (annotation.canEqual) if (annotation.value >= number) null else annotation.buildResult()
        else if (annotation.value > number) null else annotation.buildResult()
    }

    override fun messageFun(annotation: Max) = annotation.message

}

//class MaxValidator : Validator {
//    override fun validate(annotation: Annotation, bean: Any?) =
//            (annotation as? Max)?.let {
//                (bean as? Long)?.run {
//                    if (it.canEqual) it.value >= bean
//                    else it.value > bean
//                }
//            } ?: false
//}