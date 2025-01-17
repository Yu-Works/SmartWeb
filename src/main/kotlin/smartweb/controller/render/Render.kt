package smartweb.controller.render

import rain.controller.special.ActionResult
import smartweb.controller.WebActionContext
import smartweb.server.InternalWebServer
import java.lang.RuntimeException

// 自定义渲染器
fun interface Render {

    /** 渲染函数
     *
     * 如返回 null 则认为渲染器处理完成，无需后续处理。
     * 如返回任意值则将返回值作为响应内容，按照处理流程进行后续处理。
     *
     * @param context 请求上下文
     * @param server 服务器
     *
     * @return 响应内容
     */
    operator fun invoke(context: WebActionContext, server: InternalWebServer): Any?

    fun render(): Nothing = throw ActionResult(this)
}
fun render302(location: String): Nothing = Render { context, _ ->
    context.resp.status = 302
    context.resp.addHeader("Location", location)
    null
}.render()

fun render401(body: Any? = ""): Nothing = Render { context, _ ->
    context.resp.status = 401
    body
}.render()

fun render403(body: Any? = ""): Nothing = Render { context, _ ->
    context.resp.status = 403
    body
}.render()