package com.origin.net

import com.origin.CraftList
import com.origin.ObjectID
import com.origin.model.*
import com.origin.model.craft.Craft
import com.origin.model.inventory.Hand
import com.origin.model.inventory.Inventory
import com.origin.model.item.Item
import com.origin.model.item.ItemFactory
import com.origin.net.ServerPacket.*

/**
 * сообщение для клиента
 */
abstract class ServerMessage(
    @Transient
    val channel: String,
)

/**
 * ответ на авторизацию по токену
 */
data class AuthorizeTokenResponse(
    val characterId: ObjectID,
    val proto: String
)

enum class ClientPacket(val n: String) {
    MAP_CLICK("mc"),
    OBJECT_CLICK("oc"),
    OBJECT_RIGHT_CLICK("orc"),
    CHAT("chat"),
    OPEN_MY_INVENTORY("openmyinv"),
    INVENTORY_CLOSE("invclose"),
    ITEM_CLICK("itemclick"),
    ITEM_RIGHT_CLICK("itemrclick"),
    CONTEXT_MENU_SELECT("cmselect"),
    CRAFT("craft"),
    ACTION("action"),
    KEY_DOWN("kd"),
}

enum class ServerPacket(val n: String) {
    MAP_DATA("m"),
    MAP_CONFIRMED("mc"),
    OBJECT_ADD("oa"),
    OBJECT_DELETE("od"),
    OBJECT_MOVE("om"),
    OBJECT_STOP("os"),
    OBJECT_LIFT("ol"),
    STATUS_UPDATE("su"),
    ACTION_PROGRESS("ap"),
    CONTEXT_MENU("cm"),
    INVENTORY_UPDATE("iv"),
    INVENTORY_CLOSE("ic"),
    PLAYER_HAND("ph"),
    CREATURE_SAY("cs"),
    TIME_UPDATE("tu"),
    CRAFT_LIST("cl"),
    CURSOR("cu"),
}

class MapGridData(grid: Grid, flag: Type) : ServerMessage(MAP_DATA.n) {
    enum class Type {
        REMOVE, ADD, CHANGE
    }

    private val x: Int = grid.x
    private val y: Int = grid.y
    private val a: Int = flag.ordinal

    private val tiles: ByteArray? = if (a > 0) grid.tilesBlob else null
}

/**
 * говорим клиенту что у него есть все актуальные данные для перестройки всех тайлов доступных у него
 * пояснение: когда мы шлем клиенту гриды по очереди или входим в новые гриды
 * клиент перестраивает карту не сразу, это может занять какое то вермя. при вычислении "слоев" тайлов
 * нужны данные соседних гридов - иначе будут артефакты. поэтому этим сообщением мы говорим что точно отослали
 * все гриды которые должны быть у клиента и можно перестраивать карту. все будет ок
 */
class MapGridConfirm : ServerMessage(MAP_CONFIRMED.n)

class ObjectAdd(obj: GameObject) : ServerMessage(OBJECT_ADD.n) {
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
    private val a = null// if (obj is Player) obj.appearance else null
}

class ObjectDel(obj: GameObject) : ServerMessage(OBJECT_DELETE.n) {
    private val id = obj.id
}

class ObjectStartMove(m: BroadcastEvent.StartMove) : ServerMessage(OBJECT_MOVE.n) {
    private val id = m.obj.id
    private val tx = m.toX
    private val ty = m.toY
    private val x = m.obj.pos.x
    private val y = m.obj.pos.y
    private val s = m.speed
    private val mt = m.moveType
}

class ObjectMoved(m: BroadcastEvent.Moved) : ServerMessage(OBJECT_MOVE.n) {
    private val id = m.obj.id
    private val tx = m.toX
    private val ty = m.toY
    private val x = m.obj.pos.x
    private val y = m.obj.pos.y
    private val s = m.speed
    private val mt = m.moveType
}

class ObjectStopped(m: BroadcastEvent.Stopped) : ServerMessage(OBJECT_STOP.n) {
    private val id = m.obj.id
    private val x = m.obj.pos.x
    private val y = m.obj.pos.y
}


class ContextMenuData(contextMenu: ContextMenu?) : ServerMessage(CONTEXT_MENU.n) {
    // -1 очистка контекстного меню
    private val id = contextMenu?.obj?.id ?: contextMenu?.item?.id ?: -1
    private val l = contextMenu?.items
}

class InventoryItemData(item: Item) {
    private val id = item.id
    private val x = item.x
    private val y = item.y
    private val w = item.width
    private val h = item.height
    private val q = item.quality

    private val icon = item.getIcon()
}

class InventoryUpdate(inventory: Inventory) : ServerMessage(INVENTORY_UPDATE.n) {
    private val id = inventory.id
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

class InventoryClose(val id: ObjectID) : ServerMessage(INVENTORY_CLOSE.n)

class HandUpdate : ServerMessage {
    private val icon: String?
    private val mx: Int
    private val my: Int

    // взять "в руку" предмет, следует за курсором
    constructor(hand: Hand) : super(PLAYER_HAND.n) {
        icon = hand.item.getIcon()
        mx = hand.mouseX
        my = hand.mouseY
    }

    // очистка "руки" на клиенте
    constructor() : super(PLAYER_HAND.n) {
        icon = null
        mx = 0
        my = 0
    }
}

enum class ChatChannel(val id: Int) {
    GENERAL(0),
    PRIVATE(1),
    PARTY(2),
    VILLAGE(3),
    SHOUT(4),
    WORLD(5),
    ANNOUNCEMENT(6),
    SYSTEM(0xff),
}

class CreatureSay(val id: ObjectID, title: String, text: String, channel: ChatChannel) : ServerMessage(CREATURE_SAY.n) {
    private val ti = title
    private val t = text
    private val c = channel.id
}

class ActionProgress(
    // current
    val c: Int,
    // total
    val t: Int
) : ServerMessage(ACTION_PROGRESS.n)

class TimeUpdate(
    // tickCount
    val t: Long,
    // hour
    val h: Int,
    // minute
    val m: Int,
    // day
    val d: Int,
    // month
    val mm: Int,
    // night value
    val nv: Int,
    // sun value
    val sv: Int,
    // moon value
    val mv: Int
) : ServerMessage(TIME_UPDATE.n)

class CraftData(craft: Craft) {
    private val name = craft.name

    private val produced = craft.produce.map {
        CraftItemData(ItemFactory.getIcon(it.key), it.value)
    }

    private val required = craft.requiredItems.map {
        CraftItemData(ItemFactory.getIcon(it.key), it.value)
    }

    class CraftItemData(val icon: String, val count: Int)
}

class CraftListPacket(c: CraftList) : ServerMessage(CRAFT_LIST.n) {
    private val list: List<CraftData> = c.map {
        CraftData(it.value)
    }
}

class CursorPacket(cursor: Cursor) : ServerMessage(CURSOR.n) {
    private val c: String = cursor.name.lowercase()
}

/**
 * поднять/опустить объект игроком (прилинковка на клиенте, несет над собой)
 */
class LiftPacket(obj: GameObject, isLift: Boolean, owner: GameObject) : ServerMessage(OBJECT_LIFT.n) {
    // флаг поднятия или опускания объекта
    private val l = if (isLift) 1 else 0
    // owner id - ид родителя. который перетаскивает объект
    private val oid = owner.id

    // copy from object add
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
     * appearance...
     */
    private val a = null
}
