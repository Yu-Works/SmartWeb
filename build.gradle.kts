plugins {
    java
    kotlin("jvm") version "1.9.22"
}
val coreVersion = "1.0.0-DEV1"
version = "1.0.0-DEV1"


allprojects {
    val dir = projectDir.absolutePath.split(File.separator)
    val l2 = dir[dir.size - 2]
    group = if (name == "SmartWeb") "com.IceCreamQAQ.SmartWeb"
    else if (l2 == "TempleEngine") "com.IceCreamQAQ.SmartWeb.Temple"
    else if (l2 == "WebServer") "com.IceCreamQAQ.SmartWeb.Server"
    else "com.IceCreamQAQ.Yu.WebCore"

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://maven.icecreamqaq.com/repository/maven-public/")
    }

    pluginManager.apply(JavaLibraryPlugin::class.java)
    pluginManager.apply(MavenPublishPlugin::class.java)

    java {
        withSourcesJar()
    }
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>(name) {
                groupId = project.group.toString()
                artifactId = name
                version = rootProject.version.toString()

                pom {
                    name.set("Rain Java Dev Framework")
                    description.set("Rain Java Dev Framework")
                    url.set("https://github.com/IceCream-Open/Rain")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("IceCream")
                            name.set("IceCream")
                            email.set("www@withdata.net")
                        }
                    }
                    scm {
                        connection.set("")
                    }
                }
                from(components["java"])
            }
        }

        repositories {
            mavenLocal()
            maven {
                val snapshotsRepoUrl = "https://maven.icecreamqaq.com/repository/maven-snapshots/"
                val releasesRepoUrl = "https://maven.icecreamqaq.com/repository/maven-releases/"
                url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)


                credentials {

                    val mvnInfo = readMavenUserInfo("IceCream")
                    username = mvnInfo[0]
                    password = mvnInfo[1]
                }
            }
        }
    }


    dependencies {
        implementation("com.IceCreamQAQ.Rain:event:$coreVersion")
        implementation("com.IceCreamQAQ.Rain:application:$coreVersion")
        implementation("com.IceCreamQAQ.Rain:controller:$coreVersion")
        implementation("commons-fileupload:commons-fileupload:1.5")
    }

}

dependencies {
    implementation(kotlin("stdlib"))
    api("org.ehcache:ehcache:3.10.1")
}



java {
    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

//publishing {
//
//    publications {
//        create<MavenPublication>(name) {
//            groupId = group.toString()
//            artifactId = name
//            version = project.version.toString()
//
//            pom {
//                name.set("Rain Java Dev Framework")
//                description.set("Rain Java Dev Framework")
//                url.set("https://github.com/IceCream-Open/Rain")
//                licenses {
//                    license {
//                        name.set("The Apache License, Version 2.0")
//                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
//                    }
//                }
//                developers {
//                    developer {
//                        id.set("IceCream")
//                        name.set("IceCream")
//                        email.set("www@withdata.net")
//                    }
//                }
//                scm {
//                    connection.set("")
//                }
//            }
//            from(components["java"])
//        }
//    }
//
//    repositories {
//        mavenLocal()
//        maven {
//            val snapshotsRepoUrl = "https://maven.icecreamqaq.com/repository/maven-snapshots/"
//            val releasesRepoUrl = "https://maven.icecreamqaq.com/repository/maven-releases/"
//            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
//
//
//            credentials {
//
//                val mvnInfo = readMavenUserInfo("IceCream")
//                username = mvnInfo[0]
//                password = mvnInfo[1]
//            }
//        }
//    }
//
//}
fun readMavenUserInfo(id: String) =
    fileOr(
        "mavenInfo.txt",
        "${System.getProperty("user.home")}/.m2/mvnInfo-$id.txt"
    )?.readText()?.split("|") ?: arrayListOf("", "")


fun File.check() = if (this.exists()) this else null
fun fileOr(vararg file: String): File? {
    for (s in file) {
        val f = file(s)
        if (f.exists()) return f
    }
    return null
}