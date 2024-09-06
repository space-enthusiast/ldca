package io.ldca

import co.elastic.apm.attach.ElasticApmAttacher
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ldca.plugins.KafkaProducerConfig
import io.ldca.plugins.configureChat
import io.ldca.plugins.configureRouting
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.instrumentation.ktor.v2_0.server.KtorServerTracing

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
    ElasticApmAttacher.attach()
}

fun Application.module() {
    val openTelemetry: OpenTelemetry = OpenTelemetry.noop()
    install(KtorServerTracing) {
        setOpenTelemetry(openTelemetry)
    }
    val tracer : Tracer = openTelemetry.getTracer("")

    val kafkaBootStrapServers = "kafka:9092"
    val producer = KafkaProducerConfig(kafkaBootStrapServers)
    configureChat(
        producer = producer,
        kafkaBootstrapServers = kafkaBootStrapServers,
    )
    configureRouting()
}
