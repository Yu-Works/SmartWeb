package com.IceCreamQAQ.YuWeb.temple

import com.IceCreamQAQ.YuWeb.WebActionContext

interface Temple {
    fun invoke(context: WebActionContext): String
}