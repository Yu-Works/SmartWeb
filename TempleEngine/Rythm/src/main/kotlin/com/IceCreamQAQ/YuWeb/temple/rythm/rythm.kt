package com.IceCreamQAQ.YuWeb.temple.rythm

import com.IceCreamQAQ.YuWeb.WebActionContext
import com.IceCreamQAQ.YuWeb.temple.Temple
import com.IceCreamQAQ.YuWeb.temple.TempleEngine
import org.rythmengine.Rythm
import org.rythmengine.RythmEngine
import java.io.File
import javax.inject.Named

@Named("rythm")
class RythmTempleEngine : TempleEngine {

    private lateinit var engine: RythmEngine

    private var isDev = false

    override fun start(mode: String) {
        engine = RythmEngine(mapOf("engine.mode" to if (mode == "dev") Rythm.Mode.dev else Rythm.Mode.prod))
        if (mode == "dev") isDev = true
    }

    override fun close() {
        engine.shutdown()
    }

    override fun getTemple(path: String): Temple? {
        return if (isDev)
            RythmTemple(engine, File("src/main/resources/rythm/$path.html").let { if (it.exists()) it else null }
                ?: return null)
        else
            ProdRythmTemple(
                engine,
                Thread.currentThread().contextClassLoader.getResource("rythm/$path.html")?.readText() ?: return null
            )
    }

}

class RythmTemple(private val engine: RythmEngine, private val file: File) : Temple {
    override fun invoke(context: WebActionContext): String {
        context.run {
            saves["req"] = request
            saves["resp"] = response
            saves["context"] = context
        }
        return engine.render(file, context.saves)
    }
}

class ProdRythmTemple(private val engine: RythmEngine, private val temple: String) : Temple {
    override fun invoke(context: WebActionContext): String {
        context.run {
            saves["req"] = request
            saves["resp"] = response
            saves["context"] = context
        }
        return engine.render(temple, context.saves)
    }
}