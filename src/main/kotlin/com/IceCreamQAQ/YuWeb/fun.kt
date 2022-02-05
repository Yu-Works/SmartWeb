package com.IceCreamQAQ.YuWeb

import java.io.File

fun String.toParaName() = this.toLowerCase().replace("_", "").replace("-", "")

fun file(vararg names: String): File? {
    for (name in names) {
        File(name).let { if (it.exists()) return it }
    }
    return null
}