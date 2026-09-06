package smartweb.event

import smartweb.server.WebServer
import rain.api.event.Event
import rain.event.events.AbstractCancelAbleEvent
import smartweb.controller.WebActionContext

open class WebServerStatusChangedEvent(val server: WebServer) : Event {
    open class Started(server: WebServer) : WebServerStatusChangedEvent(server)
    open class Stopping(server: WebServer) : WebServerStatusChangedEvent(server)
}

open class Request404Event(val context: WebActionContext) : AbstractCancelAbleEvent()