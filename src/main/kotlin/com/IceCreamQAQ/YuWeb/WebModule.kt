package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Default
import com.IceCreamQAQ.Yu.di.YuContext
import com.IceCreamQAQ.Yu.loader.AppClassloader
import com.IceCreamQAQ.Yu.module.Module
import com.IceCreamQAQ.YuWeb.temple.TempleEngine
import javax.inject.Inject
import javax.inject.Named

class WebModule : Module {



    @Inject
    private lateinit var context: YuContext

    override fun onLoad() {

    }
}