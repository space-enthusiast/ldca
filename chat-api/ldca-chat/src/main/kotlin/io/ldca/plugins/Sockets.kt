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
import io.ldca.ChatMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.OffsetDateTime.now
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

fun Application.configureChat(
    producer: KafkaProducerConfig,
    kafkaBootstrapServers: String,
) {
    data class ChatConnectionInfo(
        val session: DefaultWebSocketServerSession,
        val kafkaConsumerCoroutine: Job,
        val messageProducerCoroutine: Job,
        val channel: Channel<String>,
    ) {
        fun close() {
            kafkaConsumerCoroutine.cancel()
            messageProducerCoroutine.cancel()
            channel.close()
        }
    }

    val sessions = ConcurrentHashMap<String, ChatConnectionInfo>()
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/api/chat/{chatRoomId}/user/{userId}") {

            // create consumer group id for each connection
            val groupId = UUID.randomUUID().toString()

            // extract chatRoomId and userId from path
            val chatRoomId = call.parameters["chatRoomId"] ?: throw IllegalArgumentException("Chat room ID is required.")
            val userId = call.parameters["userId"] ?: throw IllegalArgumentException("User ID is required.")
            println("User $userId connect initialization to chat room $chatRoomId.")

            // TODO: validate chatRoomId and userId
                // check if user is in chatroom

            // create session key chatRoomId, groupId, userId + random string that is created per connection
            val sessionKey = "chatroom-$chatRoomId-user-$userId-${getRandomString(5)}"

            // channel to communicate between kafka message consumer coroutine and websocket producer coroutine
            val messageChannel = Channel<String>()

            // create consumer config for chatRoom message subscription
            val consumerConfig = KafkaConsumerConfig(
                bootstrapServers = kafkaBootstrapServers,
                groupId = groupId,
            )

            // TODO: get topic name with chatRoomId
            val chatRoomTopic = "chatroom-$chatRoomId" // temporary use topic with prefix "chatroom-"

            // save session and
            sessions[sessionKey] = ChatConnectionInfo(
                session = this,
                kafkaConsumerCoroutine = consumerConfig.consumeMessages(
                    topic = chatRoomTopic,
                    channel = messageChannel,
                ),
                messageProducerCoroutine = launch {
                    while (true) {
                        val message = messageChannel.receive()
                        send(Frame.Text(message))
                    }
                },
                channel = messageChannel,
            )

            println("User $userId connected to chat room $chatRoomId.")

            try {
                while (true) {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            val chatMessage = ChatMessage(
                                id = UUID.randomUUID(),
                                chatRoomId = UUID.fromString(chatRoomId),
                                userId = UUID.fromString(userId),
                                message = text,
                                createdAt = now(),
                            )
                            producer.sendMessage(
                                topic = chatRoomTopic,
                                key = chatMessage.id.toString(),
                                value = chatMessage.message,
                            )
                        }
                    }
                }
            } finally {
                println("User $userId disconnected from chat room $chatRoomId.")
                println("Closing connection for session $sessionKey.")
                val connectionInfo = sessions[sessionKey]
                connectionInfo?.close()
                sessions.remove(sessionKey)
                println("Connection for session $sessionKey closed.")
            }
        }
    }

}

fun getRandomString(size: Int): String {
    val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return List(size) { charset.random() }.joinToString("")
}
