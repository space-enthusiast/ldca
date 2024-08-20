package io.ldca

import java.time.OffsetDateTime
import java.util.UUID

class ChatService {

    private val chatRooms = mutableSetOf<ChatRoom>()
    private val chatRoomUsers = mutableMapOf<UUID, MutableSet<User>>()
    private val chatMessages = mutableMapOf<UUID, MutableList<ChatMessage>>()

    fun getChatRooms(): List<ChatRoom> {
        return chatRooms.toList()
    }
    fun createChatRoom(
        chatRoomName: String,
    ): ChatRoom {
        val chatRoom = ChatRoom(
            id = UUID.randomUUID(),
            name = chatRoomName
        )
        if (chatRooms.any { it.name == chatRoomName }) {
            throw Exception("Chat room with the same name already exists")
        }
        chatRooms.add(chatRoom)
        chatRoomUsers[chatRoom.id] = mutableSetOf()
        return chatRoom
    }
    fun joinChatRoom(
        chatRoomId: UUID,
        userId: UUID,
    ) {
        val chatRoomUsers = chatRoomUsers[chatRoomId] ?: throw Exception("Chat room does not exist")
        if (chatRoomUsers.any { it.id == userId }) {
            throw Exception("User is already in the chat room")
        }
        chatRoomUsers.add(User(userId))
    }
    fun getChatRoomUsers(
        chatRoomId: UUID
    ): List<User> {
        return chatRoomUsers[chatRoomId]?.toList() ?: throw Exception("Chat room does not exist")
    }
    fun deleteChatRoom(
        chatRoomId: UUID,
    ) {
        if (chatRooms.none { it.id == chatRoomId }) {
            throw Exception("Chat room does not exist")
        }
        chatRoomUsers.remove(chatRoomId)
        chatRooms.removeIf { it.id == chatRoomId }
    }
    fun sendChatMessage(
        chatRoomId: UUID,
        userId: UUID,
        message: String,
    ) {
        val chatRoomUsers = chatRoomUsers[chatRoomId] ?: throw Exception("Chat room does not exist")
        if (chatRoomUsers.none { it.id == userId }) {
            throw Exception("User is not in the chat room")
        }
        val chatMessage = ChatMessage(
            id = UUID.randomUUID(),
            chatRoomId = chatRoomId,
            userId = userId,
            message = message,
            createdAt = OffsetDateTime.now()
        )
        chatMessages.getOrPut(chatRoomId) { mutableListOf() }.add(chatMessage)
    }
}