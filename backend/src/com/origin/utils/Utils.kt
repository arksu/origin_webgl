package com.origin.utils

import java.util.*
import java.util.concurrent.ThreadLocalRandom

object Utils {

    @JvmStatic
    fun isEmpty(s: String?): Boolean {
        return s == null || s.isEmpty()
    }

    fun generatString(len: Int): String {
        val symbols = charArrayOf(
            'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f',
            'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm',
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
        )
        return generatString(len, symbols)
    }

    fun generatString(len: Int, symbols: CharArray): String {
        val sb = StringBuilder()
        val random: Random = ThreadLocalRandom.current()
        for (i in 0 until len) {
            sb.append(symbols[random.nextInt(symbols.size)])
        }
        return sb.toString()
    }
}