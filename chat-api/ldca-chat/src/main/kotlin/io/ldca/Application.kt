package io.ldca

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ldca.plugins.configureKafkaConsumer
import io.ldca.plugins.configureKafkaProducer
import io.ldca.plugins.configureRouting
import io.ldca.plugins.configureSockets

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSockets()
    configureRouting()
    configureKafkaProducer()
    configureKafkaConsumer()
}
