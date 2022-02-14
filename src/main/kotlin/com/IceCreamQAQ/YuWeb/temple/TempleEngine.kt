package com.IceCreamQAQ.YuWeb.temple

import com.IceCreamQAQ.Yu.annotation.AutoBind

@AutoBind
interface TempleEngine {

    fun start(mode: String)
    fun close()
    fun getTemple(path: String): Temple?

}