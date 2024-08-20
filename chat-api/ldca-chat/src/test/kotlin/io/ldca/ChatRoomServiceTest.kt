package io.ldca

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class ChatRoomServiceTest : FreeSpec({
    "chatRoomService.getChatRooms()" - {
        "should return a list of chat rooms" {
            val chatRoomService = ChatService()
            val chatRooms = chatRoomService.getChatRooms()
            chatRooms shouldBe emptyList()
        }
        "should return a list of created chat rooms" {
            val chatRoomService = ChatService()
            chatRoomService.createChatRoom("chatRoom1")
            chatRoomService.createChatRoom("chatRoom2")
            val chatRooms = chatRoomService.getChatRooms()
            chatRooms.size shouldBe 2
        }
    }
    "chatRoomService.createChatRooms()" - {
        "should create a chat room" {
            val chatRoomService = ChatService()
            chatRoomService.createChatRoom("chatRoom1")
            val chatRooms = chatRoomService.getChatRooms()
            chatRooms.size shouldBe 1
            chatRooms[0].name shouldBe "chatRoom1"
        }
        "should create multiple chat rooms" {
            val chatRoomService = ChatService()
            chatRoomService.createChatRoom("chatRoom1")
            chatRoomService.createChatRoom("chatRoom2")
            val chatRooms = chatRoomService.getChatRooms()
            chatRooms.size shouldBe 2
            chatRooms[0].name shouldBe "chatRoom1"
            chatRooms[1].name shouldBe "chatRoom2"
        }
        "should prohibit chat room creation with the same name" {
            val chatRoomService = ChatService()
            chatRoomService.createChatRoom("chatRoom1")
            shouldThrow<Exception> {
                chatRoomService.createChatRoom("chatRoom1")
            }
        }
    }
    "chatRoomService.joinChatRoom()" - {
        "should join a chat room" {
            val chatRoomService = ChatService()
            chatRoomService.createChatRoom("chatRoom1")
            val chatRooms = chatRoomService.getChatRooms()
            val chatRoomId = chatRooms[0].id
            val user = User(UUID.randomUUID())
            chatRoomService.joinChatRoom(chatRoomId, user.id)
            val chatRoomUsers = chatRoomService.getChatRoomUsers(chatRoomId)
            chatRoomUsers.size shouldBe 1
            chatRoomUsers[0].id shouldBe user.id
        }
        "should prohibit joining a chat room with the same user" {
            val chatRoomService = ChatService()
            chatRoomService.createChatRoom("chatRoom1")
            val chatRooms = chatRoomService.getChatRooms()
            val chatRoomId = chatRooms[0].id
            val user = User(UUID.randomUUID())
            chatRoomService.joinChatRoom(chatRoomId, user.id)
            shouldThrow<Exception> {
                chatRoomService.joinChatRoom(chatRoomId, user.id)
            }
        }
        "should prohibit joining a chat room that does not exist" {
            val chatRoomService = ChatService()
            val user = User(UUID.randomUUID())
            shouldThrow<Exception> {
                chatRoomService.joinChatRoom(UUID.randomUUID(), user.id)
            }
        }
    }
    "chatRoomService.getChatRoomUsers()" - {
        "should return a list of chat room users" {
            val chatRoomService = ChatService()
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
        }
        "should prohibit getting chat room users from chat room that does not exist" {
            val chatRoomService = ChatService()
            shouldThrow<Exception> {
                chatRoomService.getChatRoomUsers(UUID.randomUUID())
            }
        }
    }
    "chatRoomService.deleteChatRoom()" - {
        "should delete a chat room" {
            val chatRoomService = ChatService()
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
            val chatRoomService = ChatService()
            val chatRoom = chatRoomService.createChatRoom("chatRoom1")
            val chatRoomId = chatRoom.id
            val user = User(UUID.randomUUID())
            chatRoomService.joinChatRoom(chatRoomId, user.id)
            chatRoomService.deleteChatRoom(chatRoomId)
            val chatRooms = chatRoomService.getChatRooms()
            chatRooms shouldBe emptyList()
        }
        "should prohibit deletion of chat room that does not exist" {
            val chatRoomService = ChatService()
            shouldThrow<Exception> {
                chatRoomService.deleteChatRoom(UUID.randomUUID())
            }
        }
    }
    "chatRoomService.sendChatMessage()" - {
        "should send a chat message" {
            val chatRoomService = ChatService()
            val chatRoom = chatRoomService.createChatRoom("chatRoom1")
            val chatRoomId = chatRoom.id
            val user = User(UUID.randomUUID())
            chatRoomService.joinChatRoom(chatRoomId, user.id)
            chatRoomService.sendChatMessage(chatRoomId, user.id, "Hello, world!")
        }
    }

})