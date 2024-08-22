package io.ldca.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.application
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.UUID

fun Application.configureRouting() {
    routing {
        get("/") {
            application.log.info("creating topic")
            val uuid = UUID.randomUUID()
            createTopic("chatroom_${uuid}", 1, 1)
            application.log.info("topic created")
            call.respondText("Hello World!")
        }

        get("/chatrooms") {
            call.respondText("chatrooms: \n${getTopics().joinToString("\n")}")
        }
    }
}
