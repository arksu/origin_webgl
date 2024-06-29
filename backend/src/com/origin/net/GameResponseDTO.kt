package com.origin.net

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.origin.util.StringTypeAdapter

/**
 * основной формат отправки сообщений клиенту (игровой протокол)
 */
class GameResponseDTO {
    /**
     * id запроса на который мы отвечаем этим пакетом
     */
    @SerializedName("id")
    private val id: Int

    /**
     * канал на клиенте в который идет сообщение, в случае ответа на конкретное сообщение канала нет
     * если шлем сообщение в канал то ид=0
     */
    @SerializedName("c")
    private val channel: String?

    /**
     * хитрая сериализация - строку парсим как json объект и передаем "как есть"
     */
    @SerializedName("d")
    @JsonAdapter(StringTypeAdapter::class)
    private val data: Any?

    /**
     * сообщение об ошибке если она возникла
     */
    @SerializedName("e")
    private val errorText: String?

    constructor(id: Int, data: Any?) {
        this.id = id
        this.data = data
        channel = null
        errorText = null
    }

    constructor(channel: String, data: Any) {
        id = 0
        this.data = data
        this.channel = channel
        errorText = null
    }

    constructor(errorText: String, id: Int) {
        this.id = id
        data = null
        channel = null
        this.errorText = errorText
    }

    override fun toString(): String {
        return "id=$id data=$data channel=$channel"
    }
}