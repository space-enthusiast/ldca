package io.ldca.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ldca.ChatService

fun Application.configureRouting() {
    routing {
        val chatRoomService = ChatService()
        get("/") {
            call.respondText("Hello World!")
        }

        get("/chatrooms") {
            call.respond(chatRoomService.getChatRooms())
        }

        post("/chatrooms") {
            val req = call.receive<CreateChatRoomRequest>()
            val chatRoomName = req.name
            try {
                val chatRoom = chatRoomService.createChatRoom(chatRoomName)
                call.respondText(chatRoom.name)
            } catch (e: Exception) {
                call.respondText(e.message ?: "Failed to create chat room", status = HttpStatusCode.InternalServerError)
            }
        }
    }
}
