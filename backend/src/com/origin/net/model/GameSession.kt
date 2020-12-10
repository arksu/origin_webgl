package com.origin.net.model

import com.origin.net.gsonSerializer
import io.ktor.http.cio.websocket.*

class GameSession(val connect: DefaultWebSocketSession) {
    suspend fun received(r: GameRequest) {
        if (r.target.equals("test")) {
            val n = r.data?.get("n")
            val t = r.data?.get("t")
            println(n)
            println(t)
            ack(r, "some")
        }


//        if (text.equals("bye", ignoreCase = true)) {
//            close(CloseReason(CloseReason.Codes.NORMAL, "said bye"))
//        }
    }

    suspend fun ack(req: GameRequest, d: Any? = null) {
        val response = GameResponse()
        response.id = req.id
        response.data = d
        connect.outgoing.send(Frame.Text(gsonSerializer.toJson(response)))
    }
}

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



 */