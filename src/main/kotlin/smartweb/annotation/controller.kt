package smartweb.annotation

import smartweb.controller.WebControllerLoader
import smartweb.http.HttpMethod
import rain.api.annotation.LoadBy

@LoadBy(WebControllerLoader::class)
annotation class WebController

annotation class WebAction(
    val value: String = "",
    vararg val methods: String = ["GET", "POST", "PUT", "DELETE"]
)

// 该注解用于声明某个注解为 WebAction，目标注解必须提供 value: String 属性，该属性会被认为是 路径。
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class RequestMethods(vararg val value: String)

@RequestMethods("GET")
annotation class GetAction(val value: String = "")
@RequestMethods("POST")
annotation class PostAction(val value: String = "")
@RequestMethods("PUT")
annotation class PutAction(val value: String = "")
@RequestMethods("DELETE")
annotation class DeleteAction(val value: String = "")
@RequestMethods("GET", "POST")
annotation class GetPostAction(val value: String = "")

annotation class RequestBody
annotation class RequestParam

annotation class SessionValue
annotation class CookieValue
annotation class ContextValue
