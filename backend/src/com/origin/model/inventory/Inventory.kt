package com.origin.model.inventory

import com.origin.entity.InventoryItemEntity
import com.origin.entity.InventoryItems
import com.origin.model.GameObject
import com.origin.model.Player
import com.origin.net.model.InventoryUpdate
import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

/**
 * интвентарь объектов
 * синхронизирован по актору объекта владельца инвентаря (parent)
 */
@ObsoleteCoroutinesApi
class Inventory(private val parent: GameObject) {

    /**
     * нельзя назвать id (IntEntity имеет поле id, нельзя будет указать как параметр в sql запросе)
     */
    val inventoryId: ObjectID = parent.id

    val items = ConcurrentHashMap<ObjectID, InventoryItem>()

    val title: String
        get() {
            return parent::class.java.simpleName
        }

    init {
        transaction {
            val list = InventoryItemEntity.find { InventoryItems.inventoryId eq inventoryId }
            list.forEach {
                items[it.id.value] = ItemsFactory.byEntity(this@Inventory, it)
            }
        }
    }

    fun getWidth(): Int {
        // TODO
        return 6
    }

    fun getHeight(): Int {
        // TODO
        return 4
    }

    suspend fun send(player: Player) {
        player.session.send(InventoryUpdate(this))
    }

    fun takeItem(id: ObjectID): InventoryItem? {
        // TODO broadcast
        return items.remove(id)
    }

    /**
     * положить вещь в инвентарь
     * @param x координаты куда положить вещь в инвентаре
     * @param y координаты куда положить вещь в инвентаре
     * @return удалось ли положить
     */
    fun putItem(item: InventoryItem, x: Int, y: Int): Boolean {
        // TODO проверка можно ли положить вещь в этот инвентарь
        if (x >= 0 && y >= 0) {
            return tryPut(item, x, y)
        } else {

        }

        // TODO broadcast
        return false
    }

    private fun tryPut(item: InventoryItem, x: Int, y: Int): Boolean {
        if (x + item.width <= getWidth() && y + item.height <= getHeight()) {
            var conflict = false

            for (i in items.values) {
                if (i.collide(x, y, i.width, i.height)) {
                    conflict = true
                    break
                }
            }
            if (!conflict) {
                items[item.id] = item
                item.putTo(this, x, y)

                return true
            }
        }
        return false
    }
}