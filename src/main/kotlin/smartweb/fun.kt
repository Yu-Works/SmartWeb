package smartweb

import java.io.File

class LazyObject<T : Any> {
    lateinit var value: T
}

inline fun <T, R> Iterable<T>.forEachFirst(predicate: (T) -> R?): R {
    for (element in this) predicate(element)?.let { return it }
    throw NoSuchElementException("Collection contains no element matching the predicate.")
}

inline fun <T, R> Iterable<T>.forEachFirstOrNull(predicate: (T) -> R?): R? {
    for (element in this) predicate(element)?.let { return it }
    return null
}

fun String.toParaName() = this.toLowerCase().replace("_", "").replace("-", "")

fun file(vararg names: String): File? {
    for (name in names) {
        File(name).let { if (it.exists()) return it }
    }
    return null
}

val defaultFileContentType = hashMapOf(
    // 图片资源
    "png" to "image/png",
    "gif" to "image/gif",
    "jpg" to "image/jpeg",
    "jpeg" to "image/jpeg",

    // 音频资源
    "mp3" to "audio/mp3",

    // 视频资源
    "mp4" to "video/mpeg4",

    // Web 资源
    "js" to "application/x-javascript",
    "css" to "text/css",
    "html" to "text/html",

    // 其他资源
    "txt" to "text/plain"
//    "" to "",
)

fun String.toFileContentType(): String {
    val suffix = this.substring(this.lastIndexOf(".") + 1)
    return defaultFileContentType[suffix] ?: "application/octet-stream"
}