package com.origin.util

import com.origin.error.BadRequestException
import com.origin.net.GameRequestDTO
import org.jooq.DSLContext
import java.util.*
import java.util.concurrent.ThreadLocalRandom

fun generateString(len: Int): String {
    val symbols = charArrayOf(
        'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f',
        'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm',
        '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
    )
    return generateString(len, symbols)
}

private fun generateString(len: Int, symbols: CharArray): String {
    val sb = StringBuilder()
    val random: Random = ThreadLocalRandom.current()
    for (i in 0 until len) {
        sb.append(symbols[random.nextInt(symbols.size)])
    }
    return sb.toString()
}

fun <T> DSLContext.transactionResultWrapper(block: (dsl: DSLContext) -> T): T {
    return this.transactionResult { configuration ->
        block(configuration.dsl())
    }
}

fun GameRequestDTO.getLong(name: String): Long {
    return (this.data[name] as Long?) ?: throw BadRequestException("wrong data field: $name")
}

fun GameRequestDTO.getString(name: String): String {
    return (this.data[name] as String?) ?: throw BadRequestException("wrong data field: $name")
}