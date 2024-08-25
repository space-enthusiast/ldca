package io.ldca

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.ldca.plugins.KafkaAdminClient
import io.ldca.plugins.kafkaAdminClient
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.UUID

class ChatRoomServiceTest : FreeSpec({

    val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0")).withKraft()

    beforeSpec {
        kafka.start()
        println(kafka.bootstrapServers)
        KafkaAdminClient.initialize(kafka.bootstrapServers)
        kafkaAdminClient = KafkaAdminClient.instance
    }

    afterSpec {
        kafka.stop()
    }

    val chatRoomService = ChatService()

    "chatRoomService.getChatRooms()" - {
        "should return a list of chat rooms" {
            val chatRooms = chatRoomService.getChatRooms()
            chatRooms shouldBe emptyList()
        }
        "should return a list of created chat rooms" {
            val chatRoom1 = chatRoomService.createChatRoom("chatRoom1")
            val chatRoom2 = chatRoomService.createChatRoom("chatRoom2")
            val chatRooms = chatRoomService.getChatRooms()
            chatRooms.size shouldBe 2
            chatRoomService.deleteChatRoom(chatRoom1.id)
            chatRoomService.deleteChatRoom(chatRoom2.id)
        }
    }
    "chatRoomService.createChatRooms()" - {
        "should create a chat room" {
            val chatRoom1 = chatRoomService.createChatRoom("chatRoom1")
            val chatRooms = chatRoomService.getChatRooms()
            chatRooms.size shouldBe 1
            chatRooms[0].name shouldBe "chatRoom1"
            chatRoomService.deleteChatRoom(chatRoom1.id)
        }
        "created chat room name should have a chatroom prefix" {
            val chatRoom = chatRoomService.createChatRoom("chatRoom1")
            chatRoom.kafkaTopicName.startsWith("chatroom-") shouldBe true
            chatRoomService.deleteChatRoom(chatRoom.id)
        }
        "should create multiple chat rooms" {
            val chatRoom1 = chatRoomService.createChatRoom("chatRoom1")
            val chatRoom2 = chatRoomService.createChatRoom("chatRoom2")
            val chatRooms = chatRoomService.getChatRooms()
            chatRooms.size shouldBe 2
            chatRooms[0].name shouldBe "chatRoom1"
            chatRooms[1].name shouldBe "chatRoom2"
            chatRoomService.deleteChatRoom(chatRoom1.id)
            chatRoomService.deleteChatRoom(chatRoom2.id)
        }
        "should prohibit chat room creation with the same name" {
            val chatRoom1 = chatRoomService.createChatRoom("chatRoom1")
            shouldThrow<Exception> {
                chatRoomService.createChatRoom("chatRoom1")
            }
            chatRoomService.deleteChatRoom(chatRoom1.id)
        }
    }
    "chatRoomService.joinChatRoom()" - {
        "should join a chat room" {
            val chatRoomId = chatRoomService.createChatRoom("chatRoom1").id
            val user = User(UUID.randomUUID())
            chatRoomService.joinChatRoom(chatRoomId, user.id)
            val chatRoomUsers = chatRoomService.getChatRoomUsers(chatRoomId)
            chatRoomUsers.size shouldBe 1
            chatRoomUsers[0].id shouldBe user.id
            chatRoomService.deleteChatRoom(chatRoomId)
        }
        "should prohibit joining a chat room with the same user" {
            chatRoomService.createChatRoom("chatRoom1")
            val chatRooms = chatRoomService.getChatRooms()
            val chatRoomId = chatRooms[0].id
            val user = User(UUID.randomUUID())
            chatRoomService.joinChatRoom(chatRoomId, user.id)
            shouldThrow<Exception> {
                chatRoomService.joinChatRoom(chatRoomId, user.id)
            }
            chatRoomService.unJoinChatRoom(chatRoomId, user.id)
            chatRoomService.deleteChatRoom(chatRoomId)
        }
        "should prohibit joining a chat room that does not exist" {
            val user = User(UUID.randomUUID())
            shouldThrow<Exception> {
                chatRoomService.joinChatRoom(UUID.randomUUID(), user.id)
            }
        }
    }
    "chatRoomService.getChatRoomUsers()" - {
        "should return a list of chat room users" {
            val chatRoom = chatRoomService.createChatRoom("chatRoom1")
            val chatRoomId = chatRoom.id
            val user1 = User(UUID.randomUUID())
            val user2 = User(UUID.randomUUID())
            chatRoomService.joinChatRoom(chatRoomId, user1.id)
            chatRoomService.joinChatRoom(chatRoomId, user2.id)
            val chatRoomUsers = chatRoomService.getChatRoomUsers(chatRoomId)
            chatRoomUsers.size shouldBe 2
            chatRoomUsers[0].id shouldBe user1.id
            chatRoomUsers[1].id shouldBe user2.id
            chatRoomService.unJoinChatRoom(chatRoomId, user1.id)
            chatRoomService.unJoinChatRoom(chatRoomId, user2.id)
            chatRoomService.deleteChatRoom(chatRoomId)
        }
        "should prohibit getting chat room users from chat room that does not exist" {
            shouldThrow<Exception> {
                chatRoomService.getChatRoomUsers(UUID.randomUUID())
            }
        }
    }
    "chatRoomService.deleteChatRoom()" - {
        "should delete a chat room" {
            val chatRoom = chatRoomService.createChatRoom("chatRoom1")
            val chatRoomId = chatRoom.id
            chatRoomService.deleteChatRoom(chatRoomId)
            val chatRooms = chatRoomService.getChatRooms()
            chatRooms shouldBe emptyList()
            shouldThrow<Exception> {
                chatRoomService.getChatRoomUsers(chatRoomId)
            }
        }
        "should delete a chat room with users" {
            val chatRoom = chatRoomService.createChatRoom("chatRoom1")
            val chatRoomId = chatRoom.id
            val user = User(UUID.randomUUID())
            chatRoomService.joinChatRoom(chatRoomId, user.id)
            chatRoomService.deleteChatRoom(chatRoomId)
            val chatRooms = chatRoomService.getChatRooms()
            chatRooms shouldBe emptyList()
        }
        "should prohibit deletion of chat room that does not exist" {
            shouldThrow<Exception> {
                chatRoomService.deleteChatRoom(UUID.randomUUID())
            }
        }
    }
})