package io.ldca.plugins

import kotlinx.serialization.Serializable

@Serializable
data class CreateChatRoomRequest(val name: String)