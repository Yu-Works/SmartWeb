package smartweb.server

import rain.api.annotation.AutoBind
import rain.api.permission.IUser
import smartweb.controller.WebActionContext

@AutoBind
fun interface WebUserProvider {

    operator fun invoke(context: WebActionContext): IUser?

}