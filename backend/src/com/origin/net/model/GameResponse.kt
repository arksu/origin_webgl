@file:Suppress("unused", "SpellCheckingInspection")

package com.origin.net.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.origin.model.BroadcastEvent
import com.origin.model.GameObject
import com.origin.model.Grid
import com.origin.utils.StringTypeAdapter
import kotlinx.coroutines.ObsoleteCoroutinesApi

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

abstract class ServerMessage(
    @Transient
    val channel: String,
)

/**
 * данные карты для клиента
 * @param add добавляем или удаляем грид с клиента
 */
@ObsoleteCoroutinesApi
class MapGridData(grid: Grid, add: Boolean) : ServerMessage("map") {
    val x: Int = grid.x
    val y: Int = grid.y
    val a: Int = if (add) 1 else 0
    val tiles: ByteArray? = if (add) grid.tilesBlob else null
}

@ObsoleteCoroutinesApi
class ObjectAdd(obj: GameObject) : ServerMessage("obja") {
    val id = obj.id
    val x = obj.pos.x
    val y = obj.pos.y
    val heading = obj.pos.heading
    val type: String = obj.javaClass.simpleName
}

@ObsoleteCoroutinesApi
class ObjectDel(obj: GameObject) : ServerMessage("objd") {
    val id = obj.id
}

@ObsoleteCoroutinesApi
class ObjectStartMove(m: BroadcastEvent.StartMove) : ServerMessage("objm") {
    val id = m.obj.id
    val tx = m.toX
    val ty = m.toY
    val x = m.obj.pos.x
    val y = m.obj.pos.y
    val s = m.speed
    val mt = m.moveType
}

@ObsoleteCoroutinesApi
class ObjectMoved(m: BroadcastEvent.Moved) : ServerMessage("objm") {
    val id = m.obj.id
    val tx = m.toX
    val ty = m.toY
    val x = m.obj.pos.x
    val y = m.obj.pos.y
    val s = m.speed
    val mt = m.moveType
}

@ObsoleteCoroutinesApi
class ObjectStopped(m: BroadcastEvent.Stopped) : ServerMessage("objs") {
    val id = m.obj.id
    val x = m.obj.pos.x
    val y = m.obj.pos.y
}