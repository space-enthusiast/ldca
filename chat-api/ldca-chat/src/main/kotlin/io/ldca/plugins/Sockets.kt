package io.ldca.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

fun Application.configureChat(
    producer: KafkaProducerConfig,
    kafkaBootstrapServers: String,
) {
    val groupUuid = UUID.randomUUID().toString()
    data class ChatConnectionInfo(
        val session: DefaultWebSocketServerSession,
        val consumerConfig: KafkaConsumerConfig,
    )

    val sessions = ConcurrentHashMap<String, ChatConnectionInfo>()
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/chat/{chatRoomId}/user/{userId}") {

            val chatRoomId = call.parameters["chatRoomId"] ?: throw IllegalArgumentException("Chat room ID is required.")
            val userId = call.parameters["userId"] ?: throw IllegalArgumentException("User ID is required.")
            println("User $userId connect initialization to chat room $chatRoomId.")

            val sessionKey = "ch-$chatRoomId-us-$userId"
            val consumerConfig = KafkaConsumerConfig(
                bootstrapServers = kafkaBootstrapServers,
                groupId = groupUuid,
            )
            sessions[sessionKey] = ChatConnectionInfo(
                session = this,
                consumerConfig = consumerConfig,
            )
            val chatRoomTopic = "chatroom-$chatRoomId"
            consumerConfig.consumeMessages(chatRoomTopic) { _, value ->
                this@webSocket.send(Frame.Text(value))
            }
            println("User $userId connected to chat room $chatRoomId.")

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        producer.sendMessage("chatroom-$chatRoomId", UUID.randomUUID().toString(), text)
                    }
                }
            } finally {
                sessions[sessionKey]?.consumerConfig?.close()
                sessions.remove(sessionKey)
                println("User $userId disconnected from chat room $chatRoomId.")
            }
        }
    }

}
