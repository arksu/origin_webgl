package com.origin.net

import com.origin.ObjectID
import com.origin.model.*
import com.origin.model.inventory.Inventory
import com.origin.model.inventory.InventoryItem

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

class MapGridData(grid: Grid, flag: Type) : ServerMessage("m") {
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
class MapGridConfirm : ServerMessage("mc")

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
    private val a = null// if (obj is Player) obj.appearance else null
}

class ObjectDel(obj: GameObject) : ServerMessage("od") {
    private val id = obj.id
}

class ContextMenuData(contextMenu: ContextMenu?) : ServerMessage("cm") {
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

class InventoryUpdate(inventory: Inventory) : ServerMessage("iv") {
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