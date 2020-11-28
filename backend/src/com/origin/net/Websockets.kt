package com.origin.net

/*
val wsConnections = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketSession>())

webSocket("/ws") {
    wsConnections += this
    println("onConnect")
    try {
        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()

                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "said bye"))
                    }
                }
                else -> {
                    println("uncatched frame $frame")
                }
            }
        }
    } finally {
        wsConnections -= this
    }
}

 */