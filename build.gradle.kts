plugins {
    java
    kotlin("jvm") version "1.6.10"
    `java-library`
//    `maven-publish`
}
val coreVersion = "Yu-Core:0.2.0.0-DEV18"



allprojects {
    version = "0.0.2.0-DEV20"
    group = if (name == "WebCore") "com.IceCreamQAQ.Yu"
    else "com.IceCreamQAQ.Yu.WebCore"

    repositories {
        mavenLocal()
        maven("https://maven.icecreamqaq.com/repository/maven-public/")
    }

    pluginManager.apply(JavaLibraryPlugin::class.java)
    pluginManager.apply(MavenPublishPlugin::class.java)

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>(name) {
                groupId = project.group.toString()
                artifactId = name
                version = version.toString()

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
//                from(components["java"])
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
        implementation("com.IceCreamQAQ:$coreVersion")
    }

}

dependencies {
    implementation(kotlin("stdlib"))
    api("org.smartboot.http:smart-http-server:1.1.4")
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