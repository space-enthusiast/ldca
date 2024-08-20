package io.ldca

import java.time.OffsetDateTime
import java.util.UUID

data class ChatMessage(
    val id: UUID,
    val chatRoomId: UUID,
    val userId: UUID,
    val message: String,
    val createdAt: OffsetDateTime,
)
