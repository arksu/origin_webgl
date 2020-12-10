package com.origin.net.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.origin.utils.StringTypeAdapter

class GameResponse {
    @SerializedName("id")
    var id = 0

    /**
     * хитрая сериализация - строку парсим как json объект и передаем "как есть"
     */
    @SerializedName("d")
    @JsonAdapter(StringTypeAdapter::class)
    var data: Any? = null

    @SerializedName("e")
    var errorText: String? = null

    @SerializedName("c")
    var channel: String? = null
}