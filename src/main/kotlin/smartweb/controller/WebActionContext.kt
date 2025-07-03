package smartweb.controller

import smartweb.http.HttpMethod
import smartweb.http.Request
import smartweb.http.Response
import com.alibaba.fastjson2.JSONObject
import rain.api.permission.IUser
import rain.controller.dss.PathActionContext

class WebActionContext(
    val req: Request,
    val resp: Response
) : PathActionContext() {

    override val path: Array<String> = req.path.substring(1).split('/').toTypedArray()

    internal val requestMethodEnum: HttpMethod? = runCatching { HttpMethod.valueOf(req.method) }.getOrNull()
    internal val requestMethodString: String = requestMethodEnum?.name ?: req.method

    companion object {
        internal fun WebActionContext.setUser(user: IUser) {
            this.user = user
        }
    }

    val params = JSONObject()

    var invoker: WebActionInvoker? = null

}