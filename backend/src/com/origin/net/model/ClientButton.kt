package com.origin.net.model

enum class ClientButton {
    LEFT, RIGHT, MIDDLE
}

fun getClientButton(b: Long): ClientButton {
    return when (b) {
        0L -> ClientButton.LEFT
        1L -> ClientButton.RIGHT
        2L -> ClientButton.MIDDLE
        else -> ClientButton.LEFT
    }
}