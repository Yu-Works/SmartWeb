package smartweb.temple

import rain.api.annotation.AutoBind


@AutoBind
interface TempleEngine {

    fun start(isDevMode: Boolean)
    fun close()
    fun getTemple(path: String): Temple?

}