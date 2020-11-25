package com.IceCreamQAQ.test.web

import com.IceCreamQAQ.Yu.DefaultStarter
import com.IceCreamQAQ.Yu.loader.AppClassloader
import com.alibaba.fastjson.JSONObject

fun main() {
    AppClassloader.registerTransformerList("com.IceCreamQAQ.Yu.web.WebClassTransformer")
    DefaultStarter.start()
}