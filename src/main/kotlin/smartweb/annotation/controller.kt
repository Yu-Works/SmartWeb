package smartweb.annotation

import smartweb.controller.WebControllerLoader
import smartweb.http.HttpMethod
import rain.api.annotation.LoadBy

@LoadBy(WebControllerLoader::class)
annotation class WebController

annotation class WebAction(
    val value: String,
    vararg val methods: smartweb.http.HttpMethod = [smartweb.http.HttpMethod.GET, smartweb.http.HttpMethod.POST, smartweb.http.HttpMethod.PUT, smartweb.http.HttpMethod.DELETE]
)

annotation class GetAction(val value: String)
annotation class PostAction(val value: String)
annotation class PutAction(val value: String)
annotation class DeleteAction(val value: String)

annotation class RequestBody
annotation class RequestParam

annotation class SessionValue
annotation class CookieValue
annotation class ContextValue
