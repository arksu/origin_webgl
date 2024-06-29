package com.origin.util

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 * хитрая сериализация - строку парсим как json объект и передаем "как есть"
 */
class StringTypeAdapter : JsonSerializer<Any?> {
    override fun serialize(src: Any?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        // строку передаем как есть.
        return if (src is String) {
            if (src.isNotEmpty() && src.contains("{")) {
                JsonParser.parseString(src).asJsonObject
            } else {
                context.serialize(src)
            }
        } else {
            context.serialize(src)
        }
    }
}
