package io.ldca.plugins

import io.ldca.ChatRoom
import kotlinx.serialization.Serializable

@Serializable
data class ChatRoomResults(
    val chatRooms: List<ChatRoom>
)