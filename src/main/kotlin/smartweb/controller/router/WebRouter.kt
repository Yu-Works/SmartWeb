package smartweb.controller.router

import rain.controller.Router

interface WebRouter : Router {
    val pathVars: Array<String>
}