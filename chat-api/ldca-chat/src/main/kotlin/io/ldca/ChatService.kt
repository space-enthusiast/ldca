package io.ldca

import java.util.UUID

class ChatService {
    fun getChatRooms(): List<ChatRoom> {
        TODO()
    }
    fun createChatRoom(
        chatRoomName: String,
    ): ChatRoom {
        TODO()
    }
    fun joinChatRoom(
        chatRoomId: UUID,
        userId: UUID,
    ) {
        TODO()
    }
    fun getChatRoomUsers(
        chatRoomId: UUID
    ): List<User> {
        TODO()
    }
    fun deleteChatRoom(
        chatRoomId: UUID,
    ) {
        TODO()
    }
    fun sendChatMessage(
        chatRoomId: UUID,
        userId: UUID,
        message: String,
    ) {
        TODO()
    }
}