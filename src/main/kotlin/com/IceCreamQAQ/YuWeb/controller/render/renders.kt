package com.IceCreamQAQ.YuWeb.controller.render

import com.IceCreamQAQ.YuWeb.H
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.RuntimeException

abstract class Render : RuntimeException() {
    abstract fun doRender(response: H.Response)
}

open class RenderText(private val text: String) : Render() {

    override fun doRender(response: H.Response) {
        response.body = text
    }

}

open class RenderStream(private val inputStream: InputStream) : Render() {

    open fun doHeader(response: H.Response) {

    }

    override fun doRender(response: H.Response) {
        doHeader(response)
        val l = inputStream.available()
        val bs = ByteArray(l)
        inputStream.read(bs)
        response.outputStream.write(bs)
    }
}

class RenderFile(private val file: File) : RenderStream(FileInputStream(file)) {

    override fun doHeader(response: H.Response) {
        response.contentType = "application/download"
        response.header["Content-Disposition"] = "attachment;filename=${file.name}"
        response.header["Content-Transfer-Encoding"] = "binary"

        response.contentLength = file.length()
    }

}