package com.IceCreamQAQ.test.web

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Catch
import com.IceCreamQAQ.Yu.toJSONString
import com.IceCreamQAQ.YuWeb.H
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

class Tcp {
    var tc: String? = null
    var tb: String? = null
    var ta: String? = null
}

data class Entity1(val id: Int, val name: String)
@Valid
data class Entity2(
    val id: Int,
    @field:Min(10)
    @field:Max(150)
    val height: Int
)
annotation class Permission(val value:String)
@WebController
class TestController {

    @Action("2entity")
    @Permission("user.test")
    fun entityTest(entity1: Entity1,entity2: Entity2) = arrayListOf(entity1,entity2)

//    @Action("testStudent")
//    fun testStudent(student: Student) = "你好 ${student.studentName}。"
//
//    @Action("ta")
//    fun tcp(testName: String) = "你好 $testName。"
//
    @Action("tvn")
    fun tvn(@Min(50) @Max(100) tn: Long) = "测试数值：$tn。"
//
////    @Action("tcv")
////    fun tcv(tcv: Test) = tcv
//
//    @Action("tda")
//    fun tda(testArray: DoubleArray) = "数组： ${testArray.toList()}。"
//
//    @Action("tcp")
//    fun tcp(tcp: Tcp) = tcp
//
//    @Action("tcp2")
//    fun tcp2(ta: String, tb: Int, tc: Long) = "ta: $ta, tb: $tb, tc: $tc."
//
//    @Action("tcp3")
//    fun tcp3(ta: String, tb: Int, tcp: Tcp) = "ta: $ta, tb: $tb, tcp: ${tcp.toJSONString()}."
//
//    @Action("tcp4")
//    fun tcp3(tcpA: Tcp, tcpB: Tcp) = "tcpA: ${tcpA.toJSONString()}, tcpB: ${tcpB.toJSONString()}."
//
//    @Action("hello")
//    fun hello(who: String?) = "你好 $who"
//
//    @Action("{fileName}.jpg")
//    fun image(response: H.Response, fileName: String) {
//        response.contentType = "image/jpeg"
//        val fin = FileInputStream(File("$fileName.jpg"))
//        throw RenderStream(fin)
//    }
//
//    @Catch(error = ValidateFailException::class)
//    fun validFailed(exception: Exception) {
//        println("on ")
//        exception.printStackTrace()
//    }
//
//    @Action("headCommit")
//    fun headCommit(headCommit: String) = headCommit
}

//@WebController
//@Named("test2")
//class TestController2 {
//
//    @Action("hello")
//    fun hello() = "hello test2"
//
//}