package io.ldca

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ldca.plugins.KafkaProducerConfig
import io.ldca.plugins.configureChat
import org.apache.kafka.clients.admin.NewTopic
import java.util.UUID

class ChatServiceTest: FreeSpec({
    "chat service test" - {
        "chat message send & receive test" {
            testApplication {
                val chatRoomId = UUID.randomUUID()
                application {
                    val producer = KafkaProducerConfig(kafkaTestContainer.bootstrapServers)
                    configureChat(producer, kafkaTestContainer.bootstrapServers)
                    configureKafkaAdminClient(kafkaBootStrapServers = kafkaTestContainer.bootstrapServers)
                    val adminClient = KafkaAdminClient.instance
                    val newTopic = NewTopic(
                        "chatroom-$chatRoomId",
                        1,
                        1,
                    )
                    adminClient.createTopics(listOf(newTopic)).all().get()
                }

                data class UserClient(
                    val client: HttpClient,
                    val user: User,
                )

                val user1 = UserClient(
                    client = createClient { install(WebSockets) },
                    user = User(UUID.randomUUID())
                )

                val user2 = UserClient(
                    client = createClient { install(WebSockets) },
                    user = User(UUID.randomUUID())
                )

                val message = "chat message"

                user1.client.webSocket("/api/chat/$chatRoomId/user/${user1.user.id}") {
                    send(Frame.Text(message))
                }

                var messageReceived = false
                user2.client.webSocket("/api/chat/$chatRoomId/user/${user2.user.id}") {
                    val receivedFrame = incoming.receive() as Frame.Text
                    val receivedText = receivedFrame.readText()
                    receivedText shouldBe message.also {
                        messageReceived = true
                    }
                }
                messageReceived shouldBe true
            }
        }
    }
})