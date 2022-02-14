package com.IceCreamQAQ.test.web

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Catch
import com.IceCreamQAQ.Yu.annotation.Default
import com.IceCreamQAQ.Yu.toJSONString
import com.IceCreamQAQ.YuWeb.H
import com.IceCreamQAQ.YuWeb.annotation.Output
import com.IceCreamQAQ.YuWeb.annotation.WebController
import com.IceCreamQAQ.YuWeb.controller.render.RenderStream
import com.IceCreamQAQ.YuWeb.validation.Max
import com.IceCreamQAQ.YuWeb.validation.Min
import com.IceCreamQAQ.YuWeb.validation.Valid
import com.IceCreamQAQ.YuWeb.validation.ValidateFailException
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import javax.inject.Named

@WebController
class TestController {

    @Action("helloRythm")
    fun helloRythm(@Default("World") @Output who:String) = "hello"

}