package io.ldca

import io.ldca.plugins.OffsetDateTimeSerializer
import io.ldca.plugins.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.util.UUID

@Serializable
data class ChatMessage(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val chatRoomId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val message: String,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val createdAt: OffsetDateTime,
)
