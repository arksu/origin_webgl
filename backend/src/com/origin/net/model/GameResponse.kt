@file:Suppress("unused", "SpellCheckingInspection")

package com.origin.net.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.origin.model.*
import com.origin.model.BroadcastEvent.ChatMessage.Companion.GENERAL
import com.origin.model.inventory.Hand
import com.origin.model.inventory.Inventory
import com.origin.model.inventory.InventoryItem
import com.origin.utils.GRID_SIZE
import com.origin.utils.GRID_SQUARE
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

/**
 * сообщение для клиента
 */
abstract class ServerMessage(
    @Transient
    val channel: String,
)

/**
 * данные карты для клиента, если такой грид уже есть - надо заменить в кэше клиента
 * @param flag добавляем, удаляем или изменяем грид на клиенте
 */
@ObsoleteCoroutinesApi
class MapGridData(grid: Grid, flag: Type) : ServerMessage("m") {
    enum class Type {
        REMOVE, ADD, CHANGE
    }

    private val x: Int = grid.x
    private val y: Int = grid.y
    private val a: Int = flag.ordinal

    private val tiles: ByteArray? = if (a > 0) grid.tilesBlob else null

//    private val etiles: ByteArray?

//    init {
//        if (tiles == null) {
//            etiles = null
//        } else {
//            val bt = ArrayList<Byte>(GRID_SQUARE * 2)
//            for (x in 0..GRID_SIZE) for (y in 0..GRID_SIZE) {
//                val idx = y * GRID_SIZE + x
//                val baseTile = tiles[idx]
//            }
//            etiles = ByteArray(2)
//        }
//    }
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
    private val id = obj.id
    private val x = obj.pos.x
    private val y = obj.pos.y

    /**
     * heading
     */
    private val h = obj.pos.heading

    /**
     * class name of object
     */
    private val c: String = obj.javaClass.simpleName

    /**
     * type id
     */
    private val t = if (obj is StaticObject) obj.type else 0

    /**
     * path to resource
     */
    private val r = obj.getResourcePath()

    /**
     * appearance
     */
    private val a = if (obj is Player) obj.appearance else null
}

@ObsoleteCoroutinesApi
class ObjectDel(obj: GameObject) : ServerMessage("od") {
    private val id = obj.id
}

@ObsoleteCoroutinesApi
class ObjectStartMove(m: BroadcastEvent.StartMove) : ServerMessage("om") {
    private val id = m.obj.id
    private val tx = m.toX
    private val ty = m.toY
    private val x = m.obj.pos.x
    private val y = m.obj.pos.y
    private val s = m.speed
    private val mt = m.moveType
}

@ObsoleteCoroutinesApi
class ObjectMoved(m: BroadcastEvent.Moved) : ServerMessage("om") {
    private val id = m.obj.id
    private val tx = m.toX
    private val ty = m.toY
    private val x = m.obj.pos.x
    private val y = m.obj.pos.y
    private val s = m.speed
    private val mt = m.moveType
}

@ObsoleteCoroutinesApi
class ObjectStopped(m: BroadcastEvent.Stopped) : ServerMessage("os") {
    private val id = m.obj.id
    private val x = m.obj.pos.x
    private val y = m.obj.pos.y
}

@ObsoleteCoroutinesApi
class CreatureSay(val id: ObjectID, text: String, channel: Int) : ServerMessage("cs") {
    constructor(m: BroadcastEvent.ChatMessage) : this(m.obj.id, m.text, GENERAL)

    private val t = text
    private val c = channel
}

@ObsoleteCoroutinesApi
class ContextMenuData(cm: ContextMenu?) : ServerMessage("cm") {
    private val id = cm?.obj?.id ?: -1
    private val l = cm?.items
}

class ActionProgress(val c: Int, val t: Int) : ServerMessage("ap")

@ObsoleteCoroutinesApi
class InventoryItemData(item: InventoryItem) {
    private val id = item.id
    private val x = item.x
    private val y = item.y
    private val w = item.width
    private val h = item.height
    private val q = item.q
    private val c = "Test"
    private val icon = item.icon
}

@ObsoleteCoroutinesApi
class InventoryUpdate(inventory: Inventory) : ServerMessage("iv") {
    private val id = inventory.inventoryId
    private val t = inventory.title
    private val w = inventory.getWidth()
    private val h = inventory.getHeight()
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
class FileChanged(private val f: String) : ServerMessage("fc")
class FileAdded(private val f: String) : ServerMessage("fa")
