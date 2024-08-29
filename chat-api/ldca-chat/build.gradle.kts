
val kotlin_version: String by project
val kotlin_test_version: String by project
val logback_version: String by project
val ktor_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
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
    implementation("org.apache.kafka:kafka-clients:3.8.0")
    implementation("org.apache.kafka:kafka-streams:3.8.0")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    testImplementation("org.apache.kafka:kafka-streams-test-utils:3.8.0")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotlin_test_version")
    testImplementation("org.testcontainers:testcontainers:1.20.1")
    testImplementation("org.testcontainers:kafka:1.20.1")
    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")
    testImplementation("org.testcontainers:mysql:1.20.1")
    testImplementation("mysql:mysql-connector-java:8.0.32")

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

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}