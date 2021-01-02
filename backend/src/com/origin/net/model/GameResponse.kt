package com.origin.net.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.origin.model.GameObject
import com.origin.model.Grid
import com.origin.utils.StringTypeAdapter

class GameResponse {
    @SerializedName("id")
    private val id: Int

    /**
     * хитрая сериализация - строку парсим как json объект и передаем "как есть"
     */
    @SerializedName("d")
    @JsonAdapter(StringTypeAdapter::class)
    private val data: Any?

    @SerializedName("e")
    private val errorText: String?

    @SerializedName("c")
    private val channel: String?

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

@Suppress("unused")
class MapGridData(grid: Grid) {
    val x: Int = grid.x
    val y: Int = grid.y
    val tiles: ByteArray = grid.tilesBlob
}

class ObjectPosition(obj: GameObject) {
    val id = obj.id
    val x = obj.pos.x
    val y = obj.pos.y
    val heading = obj.pos.heading
}