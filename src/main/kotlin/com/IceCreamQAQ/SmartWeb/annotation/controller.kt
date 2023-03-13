package com.IceCreamQAQ.SmartWeb.annotation

import com.IceCreamQAQ.SmartWeb.controller.WebControllerLoader
import com.IceCreamQAQ.SmartWeb.http.HttpMethod
import com.IceCreamQAQ.Yu.annotation.LoadBy

@LoadBy(WebControllerLoader::class)
annotation class WebController

annotation class WebAction(
    val value: String,
    vararg val methods: HttpMethod = [HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE]
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
