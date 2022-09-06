package com.IceCreamQAQ.test.web

import com.IceCreamQAQ.SmartWeb.annotation.NewWs
import com.IceCreamQAQ.SmartWeb.event.WebServerStatusChangedEvent
import com.IceCreamQAQ.SmartWeb.websocket.WsAction
import com.IceCreamQAQ.SmartWeb.websocket.kotlin.KWsActionCreator
import com.IceCreamQAQ.SmartWeb.websocket.kotlin.KWsActionCreator.Companion.newWs
import com.IceCreamQAQ.SmartWeb.websocket.kotlin.newWs
import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Catch
import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.toJSONString
import com.IceCreamQAQ.YuWeb.H
import com.IceCreamQAQ.YuWeb.annotation.WebController
import com.IceCreamQAQ.YuWeb.server.shttp.websocket.WsHandler
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

annotation class Permission(val value: String)

@WebController
class TestController {

    @NewWs("/hello")
    fun onWebServerStart() = newWs {
        handleText {
            println(it)
            send("你发送的是: $it。")

            attachment?.let { send("您上次发送的是: ${attachment}。") }

            attachment = it
            if (it == "999") sendToAll("转发到全部人: 999！")
        }
        handShake {
            send("欢迎链接。")
        }
    }

    @Action("tpv/{pv}")
    fun testPathVar(pv: Int?) = pv

    @Action("2entity")
    @Permission("user.test")
    fun entityTest(entity1: Entity1, entity2: Entity2) = arrayListOf(entity1, entity2)

    @Action("testDownload")
    fun testDownload() = File("pom.xml")

    //    @Action("testStudent")
//    fun testStudent(student: Student) = "你好 ${student.studentName}。"
//
//    @Action("ta")
//    fun tcp(testName: String) = "你好 $testName。"
//
    @Action("tvn")
    fun tvn(@Min(50) @Max(100) tn: Long) = "测试数值：$tn。"

    data class User(val name: String, val sex: Boolean?)

    @Action("update/{id}")
    fun update(user: User, id: Int) {

    }
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