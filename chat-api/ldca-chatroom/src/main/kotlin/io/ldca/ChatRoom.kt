package io.ldca

import java.util.UUID

data class ChatRoom(
    val id: UUID,
    val name: String,
    val kafkaTopicName: String,
)