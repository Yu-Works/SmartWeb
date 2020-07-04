package com.IceCreamQAQ.YuWeb.controller.render

import com.IceCreamQAQ.YuWeb.H
import java.io.InputStream
import java.lang.RuntimeException

abstract class Render : RuntimeException() {
    abstract fun doRender(response: H.Response)
}

class RenderText(private val text: String) : Render() {

    override fun doRender(response: H.Response) {
        response.body = text
    }

}

class RenderStream(private val inputStream: InputStream) : Render() {
    override fun doRender(response: H.Response) {
        val l = inputStream.available()
        val bs = ByteArray(l)
        inputStream.read(bs)
        response.outputStream.write(bs)
//        response.outputStream.w
    }
}