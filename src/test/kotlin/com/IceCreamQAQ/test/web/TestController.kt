package com.IceCreamQAQ.test.web

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.YuWeb.H
import com.IceCreamQAQ.YuWeb.annotation.WebController
import com.IceCreamQAQ.YuWeb.controller.render.RenderStream
import java.io.File
import java.io.FileInputStream
import javax.inject.Named

@WebController
class TestController {

    @Action("hello")
    fun hello(who: String?) = "你好 $who"

    @Action("{fileName}.jpg")
    fun image(response: H.Response,fileName:String){
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