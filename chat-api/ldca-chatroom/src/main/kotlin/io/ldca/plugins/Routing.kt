package io.ldca.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.application
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.UUID

fun Application.configureRouting() {
    routing {
        get("/") {
            application.log.info("Hello from Ktor!")
            val uuid = UUID.randomUUID().toString()
            producer.send(ProducerRecord("test", uuid, "Hello, world!".encodeToByteArray())) // Or asyncSend
            application.log.info("Sending message to Kafka")
            call.respondText("Hello World!")
        }
    }
}
