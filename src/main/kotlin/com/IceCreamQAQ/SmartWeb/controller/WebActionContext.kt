package com.IceCreamQAQ.SmartWeb.controller

import com.IceCreamQAQ.SmartWeb.http.HttpMethod
import com.IceCreamQAQ.SmartWeb.http.Request
import com.IceCreamQAQ.SmartWeb.http.Response
import com.IceCreamQAQ.Yu.controller.dss.PathActionContext
import com.alibaba.fastjson2.JSONObject

class WebActionContext(
    val requestMethod: HttpMethod,
    path: Array<String>,
    val req: Request,
    val resp: Response
) : PathActionContext(path) {

    val params = JSONObject()

    var invoker: WebActionInvoker? = null

}