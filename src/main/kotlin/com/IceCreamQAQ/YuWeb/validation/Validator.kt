package com.IceCreamQAQ.YuWeb.validation

interface Validator {

    fun validate(annotation: Annotation, bean: Any?): ValidateResult?

}