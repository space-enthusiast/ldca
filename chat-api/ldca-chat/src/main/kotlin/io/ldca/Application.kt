package io.ldca

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ldca.plugins.KafkaProducerConfig
import io.ldca.plugins.configureKafkaAdminClient
import io.ldca.plugins.configureRouting
import io.ldca.plugins.configureChat

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val kafkaBootStrapServers = "kafka:9092"
    val producer = KafkaProducerConfig(kafkaBootStrapServers)
    configureKafkaAdminClient(kafkaBootStrapServers)
    configureChat(
        producer = producer,
        kafkaBootstrapServers = kafkaBootStrapServers,
    )
    configureRouting()
}
