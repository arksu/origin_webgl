package com.origin.utils

import java.util.*

object Rnd {
    private val random = Random()

    fun next(bound: Int): Int {
        return random.nextInt(bound)
    }
}