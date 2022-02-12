plugins {
    java
    kotlin("jvm") version "1.6.10"
    `java-library`
    `maven-publish`
}

group = "com.IceCreamQAQ.Yu"
version = "0.0.2.0-DEV20"

repositories {
    mavenLocal()
    maven("https://maven.icecreamqaq.com/repository/maven-public/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.IceCreamQAQ:Yu-Core:0.2.0.0-DEV13")
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

publishing {

    publications {
        create<MavenPublication>("WebCore") {
            groupId = group.toString()
            artifactId = name
            version = project.version.toString()

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