@file:Suppress("unused", "SpellCheckingInspection")

package com.origin.net.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.origin.model.*
import com.origin.model.BroadcastEvent.ChatMessage.Companion.GENERAL
import com.origin.model.ContextMenu
import com.origin.model.inventory.Hand
import com.origin.model.inventory.Inventory
import com.origin.model.inventory.InventoryItem
import com.origin.utils.ObjectID
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
 * данные карты для клиента, если такой грид уже есть - надо заменить в кэше клиента
 * @param flag добавляем, удаляем или изменяем грид на клиенте
 */
@ObsoleteCoroutinesApi
class MapGridData(grid: Grid, flag: Int) : ServerMessage("m") {
    val x: Int = grid.x
    val y: Int = grid.y
    val a: Int = flag
    val tiles: ByteArray? = if (flag > 0) grid.tilesBlob else null
}

/**
 * говорим клиенту что у него есть все актуальные данные для перестройки всех тайлов доступных у него
 * пояснение: когда мы шлем клиенту гриды по очереди или входим в новые гриды
 * клиент перестраивает карту не сразу, это может занять какое то вермя. при вычислении "слоев" тайлов
 * нужны данные соседних гридов - иначе будут артефакты. поэтому этим сообщением мы говорим что точно отослали
 * все гриды которые должны быть у клиента и можно перестраивать карту. все будет ок
 */
class MapGridConfirm : ServerMessage("mc")

@ObsoleteCoroutinesApi
class ObjectAdd(obj: GameObject) : ServerMessage("oa") {
    val id = obj.id
    val x = obj.pos.x
    val y = obj.pos.y

    /**
     * heading
     */
    val h = obj.pos.heading

    /**
     * class name of object
     */
    val c: String = obj.javaClass.simpleName

    /**
     * type id
     */
    val t = if (obj is StaticObject) obj.type else 0

    /**
     * path to resource
     */
    val r = obj.getResourcePath()

    /**
     * appearance
     */
    val a = if (obj is Player) obj.appearance else null
}

@ObsoleteCoroutinesApi
class ObjectDel(obj: GameObject) : ServerMessage("od") {
    val id = obj.id
}

@ObsoleteCoroutinesApi
class ObjectStartMove(m: BroadcastEvent.StartMove) : ServerMessage("om") {
    val id = m.obj.id
    val tx = m.toX
    val ty = m.toY
    val x = m.obj.pos.x
    val y = m.obj.pos.y
    val s = m.speed
    val mt = m.moveType
}

@ObsoleteCoroutinesApi
class ObjectMoved(m: BroadcastEvent.Moved) : ServerMessage("om") {
    val id = m.obj.id
    val tx = m.toX
    val ty = m.toY
    val x = m.obj.pos.x
    val y = m.obj.pos.y
    val s = m.speed
    val mt = m.moveType
}

@ObsoleteCoroutinesApi
class ObjectStopped(m: BroadcastEvent.Stopped) : ServerMessage("os") {
    val id = m.obj.id
    val x = m.obj.pos.x
    val y = m.obj.pos.y
}

@ObsoleteCoroutinesApi
class CreatureSay(val id: ObjectID, text: String, channel: Int) : ServerMessage("cs") {
    constructor(m: BroadcastEvent.ChatMessage) : this(m.obj.id, m.text, GENERAL)

    val t = text
    val c = channel
}

@ObsoleteCoroutinesApi
class ContextMenuData(cm: ContextMenu?) : ServerMessage("cm") {
    val id = cm?.obj?.id ?: -1
    val l = cm?.items
}

class ActionProgress(val c: Int, val t: Int) : ServerMessage("ap")

@ObsoleteCoroutinesApi
class InventoryItemData(item: InventoryItem) {
    val id = item.id
    val x = item.x
    val y = item.y
    val w = item.width
    val h = item.height
    val q = item.q
    val c = "Test"
    val icon = item.icon
}

@ObsoleteCoroutinesApi
class InventoryUpdate(inventory: Inventory) : ServerMessage("iv") {
    val id = inventory.inventoryId
    val t = inventory.title
    val w = inventory.getWidth()
    val h = inventory.getHeight()
    private val l = ArrayList<InventoryItemData>()

    init {
        inventory.items.values.forEach {
            l.add(InventoryItemData(it))
        }
    }
}

class InventoryClose(val id: ObjectID) : ServerMessage("ic")

@ObsoleteCoroutinesApi
class HandUpdate : ServerMessage {
    private val icon: String?
    private val mx: Int
    private val my: Int

    constructor(hand: Hand) : super("ph") {
        icon = hand.item.icon
        mx = hand.mouseX
        my = hand.mouseY
    }

    constructor() : super("ph") {
        icon = null
        mx = 0
        my = 0
    }
}

/**
 * уведомления об изменениях в папке с ассетами
 */
class FileChanged(val f: String) : ServerMessage("fc")
class FileAdded(val f: String) : ServerMessage("fa")
