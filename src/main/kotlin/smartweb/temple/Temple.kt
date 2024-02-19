package smartweb.temple

import smartweb.controller.WebActionContext


interface Temple {
    fun invoke(context: WebActionContext): String
}