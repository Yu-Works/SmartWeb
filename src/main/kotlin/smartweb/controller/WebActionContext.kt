package smartweb.controller

import smartweb.http.HttpMethod
import smartweb.http.Request
import smartweb.http.Response
import com.alibaba.fastjson2.JSONObject
import rain.api.permission.IUser
import rain.controller.dss.PathActionContext

class WebActionContext(
    val requestMethod: HttpMethod,
    path: Array<String>,
    val req: Request,
    val resp: Response
) : PathActionContext(path) {

    companion object {
        internal fun WebActionContext.setUser(user: IUser) {
            this.user = user
        }
    }

    val params = JSONObject()

    var invoker: WebActionInvoker? = null

}