package io.ldca.plugins

import io.ldca.ChatMessage
import java.util.UUID

class ChatService {
    fun sendChatMessage(
        chatRoomId: UUID,
        userId: UUID,
        message: String,
    ): ChatMessage {
        TODO()
    }
    fun receiveMessage(
        chatRoomId: UUID,
        userId: UUID,
    ): ChatMessage? {
        TODO()
    }
}