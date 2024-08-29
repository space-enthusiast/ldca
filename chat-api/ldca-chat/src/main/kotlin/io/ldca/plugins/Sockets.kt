package io.ldca.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

fun Application.configureChat(
    producer: KafkaProducerConfig,
    kafkaBootstrapServers: String,
) {
    data class ChatConnectionInfo(
        val session: DefaultWebSocketServerSession,
        val consumerCoroutine: Job,
        val channel: Channel<String>,
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

            val groupUuid = UUID.randomUUID().toString()

            val chatRoomId = call.parameters["chatRoomId"] ?: throw IllegalArgumentException("Chat room ID is required.")
            val userId = call.parameters["userId"] ?: throw IllegalArgumentException("User ID is required.")
            println("User $userId connect initialization to chat room $chatRoomId.")

            val sessionKey = "ch-$chatRoomId-us-$userId"
            val messageChannel = Channel<String>()
            val consumerConfig = KafkaConsumerConfig(
                bootstrapServers = kafkaBootstrapServers,
                groupId = groupUuid,
            )
            val chatRoomTopic = "chatroom-$chatRoomId"
            sessions[sessionKey] = ChatConnectionInfo(
                session = this,
                consumerCoroutine = consumerConfig.consumeMessages(chatRoomTopic, channel = messageChannel),
                channel = messageChannel,
            )
            val messageReceiverJob = launch {
                while (true) {
                    val message = messageChannel.receive()
                    send(Frame.Text(message))
                    println("send message to channel $message")
                }
            }
            println("User $userId connected to chat room $chatRoomId.")

            try {
                while (true) {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            producer.sendMessage("chatroom-$chatRoomId", UUID.randomUUID().toString(), text)
                        }
                    }
                }
            } finally {
                sessions[sessionKey]?.consumerCoroutine?.cancel()
                sessions[sessionKey]?.channel?.close()
                messageReceiverJob.cancel()
                sessions.remove(sessionKey)
                println("User $userId disconnected from chat room $chatRoomId.")
            }
        }
    }

}
