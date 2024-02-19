package smartweb.controller.render

import smartweb.controller.WebActionContext
import smartweb.server.InternalWebServer
import java.lang.RuntimeException

abstract class Render : RuntimeException() {
    abstract fun doRender(context: WebActionContext, server: InternalWebServer)
}