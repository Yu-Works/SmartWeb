package smartweb.event

import smartweb.server.WebServer
import rain.api.event.Event

open class WebServerStatusChangedEvent(val server: WebServer) : Event {
    open class Started(server: WebServer) : WebServerStatusChangedEvent(server)
    open class Stopping(server: WebServer) : WebServerStatusChangedEvent(server)
}