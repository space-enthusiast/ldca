package io.ldca

import io.ldca.plugins.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ChatRoom(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val kafkaTopicName: String,
)