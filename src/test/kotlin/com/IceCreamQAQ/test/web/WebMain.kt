package com.IceCreamQAQ.test.web

import com.IceCreamQAQ.Yu.DefaultStarter
import com.IceCreamQAQ.Yu.loader.AppClassloader
import com.IceCreamQAQ.YuWeb.validation.ClassValidatorCreator
import com.alibaba.fastjson.JSONObject

data class Student(val studentName:String)

fun main() {
//    AppClassloader.registerBackList(arrayListOf("com.IceCreamQAQ.Yu.web.WebClassTransformer"))
    DefaultStarter.start()

//    ClassValidatorCreator().spawnClassValidator(test.Test::class.java)

//    val jo = JSONObject()
//    jo["student_name"] = "abc"
//    jo.getString()
//    println(jo.getString("studentName"))
//    println(jo.toJavaObject(Student::class.java))
}