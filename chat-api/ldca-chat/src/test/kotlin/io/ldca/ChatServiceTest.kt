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
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties
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
                        3,
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

        "partition key consistency test" {
            val chatRoomId = UUID.randomUUID().toString()
            val topic = "chatroom-$chatRoomId"
            val messageCount = 10

            val adminProps = Properties().apply {
                put("bootstrap.servers", kafkaTestContainer.bootstrapServers)
            }
            val adminClient = AdminClient.create(adminProps)
            adminClient.createTopics(listOf(NewTopic(topic, 3, 1))).all().get()

            val producerProps = Properties().apply {
                put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaTestContainer.bootstrapServers)
                put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
                put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            }
            val producer = KafkaProducer<String, String>(producerProps)

            val partitions = (1..messageCount).map { i ->
                val record = ProducerRecord(topic, chatRoomId, "message-$i")
                producer.send(record).get().partition()
            }

            producer.close()
            adminClient.close()

            partitions.size shouldBe messageCount
            partitions.distinct().size shouldBe 1
        }
    }
})
