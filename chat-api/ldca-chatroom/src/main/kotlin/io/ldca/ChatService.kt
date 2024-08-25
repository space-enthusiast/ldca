package io.ldca

import io.ldca.plugins.createTopic
import io.ldca.plugins.deleteTopic
import java.util.UUID

class ChatService {

    private val chatRooms = mutableSetOf<ChatRoom>()
    private val chatRoomUsers = mutableMapOf<UUID, MutableSet<User>>()

    fun getChatRooms(): List<ChatRoom> {
        return chatRooms.toList()
    }
    fun createChatRoom(
        chatRoomName: String,
    ): ChatRoom {
        if (chatRooms.any { it.name == chatRoomName }) {
            throw Exception("Chat room with the same name already exists")
        }
        val chatRoom = ChatRoom(
            id = UUID.randomUUID(),
            name = chatRoomName,
            kafkaTopicName = "chatroom-${UUID.randomUUID()}".also { createTopic(
                topicName = it,
                numPartitions = 1,
                replicationFactor = 1
            ) }
        )
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
    fun unJoinChatRoom(
        chatRoomId: UUID,
        userId: UUID,
    ) {
        val chatRoomUsers = chatRoomUsers[chatRoomId] ?: throw Exception("Chat room does not exist")
        if (chatRoomUsers.none { it.id == userId }) {
            throw Exception("User is not in the chat room")
        }
        chatRoomUsers.removeIf { it.id == userId }
    }
    fun getChatRoomUsers(
        chatRoomId: UUID
    ): List<User> {
        return chatRoomUsers[chatRoomId]?.toList() ?: throw Exception("Chat room does not exist")
    }
    fun deleteChatRoom(
        chatRoomId: UUID,
    ) {
        val kafkaTopicName = getChatRoom(chatRoomId).kafkaTopicName
        chatRoomUsers.remove(chatRoomId)
        deleteTopic(kafkaTopicName)
        chatRooms.removeIf { it.id == chatRoomId }
    }
    fun getChatRoom(chatRoomId: UUID): ChatRoom {
        return chatRooms.find { it.id == chatRoomId } ?: throw Exception("Chat room does not exist")
    }
}