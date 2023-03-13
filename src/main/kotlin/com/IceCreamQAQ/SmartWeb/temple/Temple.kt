package com.IceCreamQAQ.SmartWeb.temple

import com.IceCreamQAQ.SmartWeb.controller.WebActionContext


interface Temple {
    fun invoke(context: WebActionContext): String
}