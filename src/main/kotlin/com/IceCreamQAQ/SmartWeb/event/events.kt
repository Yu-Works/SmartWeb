package com.IceCreamQAQ.SmartWeb.event

import com.IceCreamQAQ.SmartWeb.server.WebServer
import com.IceCreamQAQ.Yu.event.events.Event

open class WebServerStatusChangedEvent(val server: WebServer) : Event() {
    open class Started(server: WebServer) : WebServerStatusChangedEvent(server)
    open class Stopping(server: WebServer) : WebServerStatusChangedEvent(server)
}