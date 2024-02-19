package smartweb.controller

import smartweb.http.HttpMethod
import smartweb.http.Request
import smartweb.http.Response
import com.alibaba.fastjson2.JSONObject
import rain.controller.dss.PathActionContext

class WebActionContext(
    val requestMethod: smartweb.http.HttpMethod,
    path: Array<String>,
    val req: smartweb.http.Request,
    val resp: smartweb.http.Response
) : PathActionContext(path) {

    val params = JSONObject()

    var invoker: WebActionInvoker? = null

}