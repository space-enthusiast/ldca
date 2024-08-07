
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.12"
}

group = "io.ldca"
version = "0.0.1"

application {
    mainClass.set("io.ldca.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-websockets-jvm")
    implementation("io.github.flaxoos:ktor-server-kafka-jvm:1.2.9")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

ktor {
    docker {
        localImageName.set("ldca-chat")
        imageTag.set("0.0.1")
        jreVersion.set(JavaVersion.VERSION_21)
        portMappings.set(listOf(
            io.ktor.plugin.features.DockerPortMapping(
                8081,
                8081,
                io.ktor.plugin.features.DockerPortMappingProtocol.TCP
            )
        ))
    }
}