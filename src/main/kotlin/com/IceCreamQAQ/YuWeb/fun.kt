package com.IceCreamQAQ.YuWeb

import java.io.File

fun String.toParaName() = this.toLowerCase().replace("_", "").replace("-", "")

fun file(vararg names: String): File? {
    for (name in names) {
        File(name).let { if (it.exists()) return it }
    }
    return null
}

val defaultFileContentType = hashMapOf(
    // 图片资源
    "png" to "mage/png",
    "gif" to "image/gif",
    "jpg" to "mage/jpeg",
    "jpeg" to "mage/jpeg",

    // 音频资源
    "mp3" to "audio/mp3",

    // 视频资源
    "mp4" to "video/mpeg4",

    // Web 资源
    "js" to "application/x-javascript",
    "css" to "text/css",
    "html" to "text/html",
//    "" to "",
)