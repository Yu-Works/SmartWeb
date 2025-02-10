plugins {
    java
    kotlin("jvm") version "2.0.10"
}
val coreVersion = "1.0.0-DEV2"
version = "1.0.0-DEV3"


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
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    java {
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlin {
        jvmToolchain(8)
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
                    System.getenv("MAVEN_USER")?.let { username = it }
                    System.getenv("MAVEN_TOKEN")?.let { password = it }
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
    api("org.ehcache:ehcache:3.10.8")
}