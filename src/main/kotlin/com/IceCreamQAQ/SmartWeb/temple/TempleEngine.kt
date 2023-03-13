package com.IceCreamQAQ.SmartWeb.temple

import com.IceCreamQAQ.Yu.annotation.AutoBind

@AutoBind
interface TempleEngine {

    fun start(isDevMode: Boolean)
    fun close()
    fun getTemple(path: String): Temple?

}