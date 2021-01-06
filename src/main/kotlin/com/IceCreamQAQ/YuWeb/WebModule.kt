package com.IceCreamQAQ.YuWeb

import com.IceCreamQAQ.Yu.loader.AppClassloader
import com.IceCreamQAQ.Yu.module.Module
import javax.inject.Inject
import javax.inject.Named

class WebModule : Module {

    @Inject
    @field:Named("appClassLoader")
    private lateinit var classLoader: ClassLoader

    override fun onLoad() {
        (classLoader as AppClassloader).registerTransformer("com.IceCreamQAQ.Yu.web.WebClassTransformer")
    }
}