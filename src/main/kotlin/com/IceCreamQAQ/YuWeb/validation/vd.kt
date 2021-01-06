package com.IceCreamQAQ.YuWeb.validation

import java.lang.reflect.Field

data class ValidateData(
        val annotation: Annotation,
        val Validator: Validator,
)

data class ParaValidateData(
        val field: Field,
        val annotation: Annotation,
        val Validator: Validator,
)