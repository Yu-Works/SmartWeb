package smartweb.annotation

import smartweb.controller.WebControllerLoader
import smartweb.http.HttpMethod
import rain.api.annotation.LoadBy

@LoadBy(WebControllerLoader::class)
annotation class WebController

annotation class WebAction(
    val value: String = "",
    vararg val methods: HttpMethod = [HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE]
)

annotation class GetAction(val value: String = "")
annotation class PostAction(val value: String = "")
annotation class PutAction(val value: String = "")
annotation class DeleteAction(val value: String = "")

annotation class RequestBody
annotation class RequestParam

annotation class SessionValue
annotation class CookieValue
annotation class ContextValue
