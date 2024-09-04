package io.ldca

import co.elastic.apm.attach.ElasticApmAttacher
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ldca.plugins.configureKafkaAdminClient
import io.ldca.plugins.configureRouting


fun main() {
    embeddedServer(Netty, port = 8082, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
    ElasticApmAttacher.attach()
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    configureRouting()
    configureKafkaAdminClient()
}
