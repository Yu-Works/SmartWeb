package com.IceCreamQAQ.SmartWeb.controller.render

import com.IceCreamQAQ.SmartWeb.controller.WebActionContext
import com.IceCreamQAQ.SmartWeb.server.InternalWebServer
import java.lang.RuntimeException

abstract class Render : RuntimeException() {
    abstract fun doRender(context: WebActionContext, server: InternalWebServer)
}