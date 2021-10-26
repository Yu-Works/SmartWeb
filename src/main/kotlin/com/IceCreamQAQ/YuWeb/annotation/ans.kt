package com.IceCreamQAQ.YuWeb.annotation

import com.IceCreamQAQ.Yu.annotation.EnchantBy
import com.IceCreamQAQ.Yu.annotation.LoadBy
import com.IceCreamQAQ.Yu.loader.enchant.MethodParaNamedEnchanter
import com.IceCreamQAQ.YuWeb.WebControllerLoader

@LoadBy(WebControllerLoader::class)
@EnchantBy(MethodParaNamedEnchanter::class)
annotation class WebController

annotation class RequestParameter
annotation class RequestBody