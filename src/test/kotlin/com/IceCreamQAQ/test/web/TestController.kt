package com.IceCreamQAQ.test.web

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.toJSONString
import com.IceCreamQAQ.YuWeb.H
import com.IceCreamQAQ.YuWeb.annotation.WebController
import com.IceCreamQAQ.YuWeb.controller.render.RenderStream
import java.io.File
import java.io.FileInputStream
import javax.inject.Named

class Tcp {
    var tc: String? = null
    var tb: String? = null
    var ta: String? = null
}

@WebController
class TestController {

    @Action("tcp")
    fun tcp(tcp: Tcp) = tcp

    @Action("tcp2")
    fun tcp2(ta: String, tb: Int, tc: Long) = "ta: $ta, tb: $tb, tc: $tc."

    @Action("tcp3")
    fun tcp3(ta: String, tb: Int, tcp: Tcp) = "ta: $ta, tb: $tb, tcp: ${tcp.toJSONString()}."

    @Action("tcp4")
    fun tcp3(tcpA: Tcp, tcpB: Tcp) = "tcpA: ${tcpA.toJSONString()}, tcpB: ${tcpB.toJSONString()}."

    @Action("hello")
    fun hello(who: String?) = "你好 $who"

    @Action("{fileName}.jpg")
    fun image(response: H.Response, fileName: String) {
        response.contentType = "image/jpeg"
        val fin = FileInputStream(File("$fileName.jpg"))
        throw RenderStream(fin)
    }

}

@WebController
@Named("test2")
class TestController2 {

    @Action("hello")
    fun hello() = "hello test2"

}