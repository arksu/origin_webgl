package com.origin.net

import com.origin.ObjectID
import com.origin.model.ContextMenu
import com.origin.model.GameObject
import com.origin.model.Grid
import com.origin.model.StaticObject
import com.origin.model.inventory.Hand
import com.origin.model.inventory.Inventory
import com.origin.model.inventory.InventoryItem
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
    OBJECT_RIGHT_CLICK("orc")
}

enum class ServerPacket(val n: String) {
    MAP_DATA("m"),
    MAP_CONFIRMED("mc"),
    OBJECT_ADD("oa"),
    OBJECT_DELETE("od"),
    OBJECT_MOVE("om"),
    OBJECT_STOP("os"),
    STATUS_UPDATE("su"),
    ACTION_PROGRESS("ap"),
    CONTEXT_MENU("cm"),
    INVENTORY_UPDATE("iv"),
    INVENTORY_CLOSE("ic"),
    PLAYER_HAND("ph"),
    CREATURE_SAY("cs"),
    TIME_UPDATE("tu"),
    CRAFT_LIST("cl")
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

class ContextMenuData(contextMenu: ContextMenu?) : ServerMessage(CONTEXT_MENU.n) {
    // -1 очистка контекстного меню
    private val id = contextMenu?.obj?.id ?: -1
    private val l = contextMenu?.items
}

class InventoryItemData(item: InventoryItem) {
    private val id = item.id
    private val x = item.x
    private val y = item.y
    private val w = item.width
    private val h = item.height
    private val q = item.q

    private val icon = item.icon
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

class HandUpdate : ServerMessage {
    private val icon: String?
    private val mx: Int
    private val my: Int

    // взять "в руку" предмет, следует за курсором
    constructor(hand: Hand) : super(PLAYER_HAND.n) {
        icon = hand.item.icon
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