rootProject.name = "SmartWeb"

fun includeProject(name: String, dir: String? = null){
    include(name)
    dir?.let { project(name).projectDir = file(it) }
}
// HTTP Server
includeProject(":SmartHTTP","WebServer/SmartHTTP")
includeProject(":Undertow","WebServer/Undertow")

// TempleEngine
includeProject(":Rythm","TempleEngine/Rythm")