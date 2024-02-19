package com.IceCreamQAQ.test.web

import com.IceCreamQAQ.SmartWeb.annotation.PostAction
import com.IceCreamQAQ.SmartWeb.annotation.WebAction
import com.IceCreamQAQ.SmartWeb.annotation.WebController
import com.IceCreamQAQ.SmartWeb.http.UploadFile
import java.io.File

class Tcp {
    var tc: String? = null
    var tb: String? = null
    var ta: String? = null
}

data class Entity1(val id: Int, val name: String)

data class Entity2(
    val id: Int,
    val height: Int
)

annotation class Permission(val value: String)

@WebController
class TestController {

//    @NewWs("/hello")
//    fun onWebServerStart() = newWs {
//        handleText {
//            println(it)
//            send("你发送的是: $it。")
//
//            attachment?.let { send("您上次发送的是: ${attachment}。") }
//
//            attachment = it
//            if (it == "999") sendToAll("转发到全部人: 999！")
//        }
//        handShake {
//            send("欢迎链接。")
//        }
//    }

    @WebAction("tpv/{pv}")
    fun testPathVar(pv: Int?) = pv ?: -1

    @WebAction("2entity")
    @Permission("user.test")
    fun entityTest(entity1: Entity1, entity2: Entity2) = arrayListOf(entity1, entity2)

    @WebAction("testDownload")
    fun testDownload() = File("pom.xml")

    //    @Action("testStudent")
//    fun testStudent(student: Student) = "你好 ${student.studentName}。"
//
//    @Action("ta")
//    fun tcp(testName: String) = "你好 $testName。"
//
    @WebAction("tvn")
    fun tvn(tn: Long) = "测试数值：$tn。"

    data class User(val name: String, val sex: Boolean?)

    @WebAction("update/{id}")
    fun update(user: User, id: Int) {
        println("update: $user, $id")
    }

    @PostAction("testUpload")
    fun testUpload(file: UploadFile) = file
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