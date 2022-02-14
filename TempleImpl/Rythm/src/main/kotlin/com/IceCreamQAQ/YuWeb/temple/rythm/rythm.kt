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

    override fun start(mode: String) {
        engine = RythmEngine(mapOf("engine.mode" to if (mode == "dev") Rythm.Mode.dev else Rythm.Mode.prod))
    }

    override fun close() {
        engine.shutdown()
    }

    override fun getTemple(path: String): Temple? {
        return RythmTemple(
            engine,
            File(
                Thread.currentThread().contextClassLoader.getResource("rythm/$path.html")?.toURI()
                    ?: return null
            )
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