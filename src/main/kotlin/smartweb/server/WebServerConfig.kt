package smartweb.server

import smartweb.controller.WebRootRouter
import smartweb.http.Session

data class WebServerConfig(
    val isDevMode: Boolean,
    val name: String,
    val port: Int,
    val cors: String?,
    val upload: WebServerUploadConfig,
    val rootRouter: WebRootRouter,
    val sessionCache: EhcacheHelp<smartweb.http.Session>,
    val userProvider: WebUserProvider?
)