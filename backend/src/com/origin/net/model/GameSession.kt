package com.origin.net.model

import io.ktor.http.cio.websocket.*

class GameSession(val connect: DefaultWebSocketSession)

/*
    fun send(channel: String?, data: Any?) {
        if (connect != null && connect.isOpen) {
            val response = WSResponse()
            response.id = 0
            response.data = data
            response.channel = channel
            connect.send(WSServer.gsonSerialize.toJson(response))
        }
    }

    fun sendPing(data: String?) {
        if (connect != null && connect.isOpen) {
            connect.send(data)
        }
    }

 */