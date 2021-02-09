package com.origin.net.model

enum class ClientButton {
    LEFT, RIGHT, MIDDLE
}

const val SHIFT_KEY = 1
const val ALT_KEY = 2
const val CTRL_KEY = 4
const val META_KEY = 8

fun getClientButton(b: Long): ClientButton {
    return when (b) {
        0L -> ClientButton.LEFT
        1L -> ClientButton.RIGHT
        2L -> ClientButton.MIDDLE
        else -> ClientButton.LEFT
    }
}