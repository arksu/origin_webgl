package com.origin.model.inventory

import com.origin.entity.InventoryItemEntity
import com.origin.entity.InventoryItems
import com.origin.model.GameObject
import com.origin.model.Player
import com.origin.model.objects.containers.Container
import com.origin.net.model.InventoryUpdate
import com.origin.utils.ObjectID
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

/**
 * интвентарь объектов
 * синхронизирован по актору объекта владельца инвентаря (parent)
 */
@DelicateCoroutinesApi
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
            // при загрузке учтем ограничение слотов
            InventoryItemEntity
                .find { (InventoryItems.inventoryId eq inventoryId) and (InventoryItems.x less 200) and (InventoryItems.y less 200) and (InventoryItems.deleted eq false) }
                .forEach {
                    items[it.id.value] = InventoryItem(it, this@Inventory)
                }
        }
    }

    fun getWidth(): Int {
        // TODO get inventory size
        if (parent is Player) {
            return 4
        }
        return 6
    }

    fun getHeight(): Int {
        // TODO get inventory size
        if (parent is Player) {
            return 4
        }
        return 5
    }

    /**
     * отправить игроку содержимое данного инвентаря
     * @param player какому игроку отправить содержимое этого инвентаря
     */
    suspend fun send(player: Player) {
        player.session.send(InventoryUpdate(this))
    }

    /**
     * уведомить родителя об изменениях в инвентаре
     */
    private suspend fun notify() {
        when (parent) {
            is Player -> {
                send(parent)
            }
            is Container -> {
                parent.inventoryChanged()
            }
        }
    }

    /**
     * найти и взять вещи по списку указанного типа и количества у себя и в дочерних инвентарях
     * вернем либо список взятых вещей из инвентаря при этом они будут удалены
     * либо null если не нашли все необходимые вещи
     * @param isTake надо ли забирать из инвентаря вещи
     */
    suspend fun findAndTakeItem(list: List<ItemWithCount>, isTake: Boolean = true): List<InventoryItem>? {
        val result = ArrayList<InventoryItem>(8)
        val req = RequiredList(list)
        // идем по всем вещам в инвентаре
        items.forEach {
            // проверим что такая вещь есть в списке требований
            if (req.checkAndDecrement(it.value.type)) {
                // добавим ее в результат
                result.add(it.value)
            }
        }

        // прошли по всему инвентарю - но вещи в требованиях еще остались.
        if (!req.isEmpty()) {
            return null
        }

        // список требований удовлетворен - удаляем вещи из инвентаря
        if (isTake) {
            result.forEach {
                items.remove(it.id)
            }

            notify()
        }

        // вернем список вещей которые взяли и удалили из инвентаря
        return result
    }

    suspend fun takeItem(id: ObjectID): InventoryItem? {
        val removed = items.remove(id)
        if (removed != null) {
            notify()
        }

        return removed
    }

    /**
     * положить вещь в инвентарь
     * @param x координаты куда положить вещь в инвентаре
     * @param y координаты куда положить вещь в инвентаре
     * @return удалось ли положить
     */
    suspend fun putItem(item: InventoryItem, x: Int, y: Int): Boolean {
        val result = tryPut(item, x, y)
        if (result) {
            notify()
        }
        return result
    }

    suspend fun putItem(item: InventoryItem): Boolean {
        for (iy in 0 until getHeight()) for (ix in 0 until getWidth())
            if (tryPut(item, ix, iy)) {
                notify()

                return true
            }
        return false
    }

    /**
     * проверка можно ли положить вещь в этот инвентарь
     */
    private fun tryPut(item: InventoryItem, x: Int, y: Int): Boolean {
        if (x >= 0 && y >= 0 && x + item.width <= getWidth() && y + item.height <= getHeight()) {
            var conflict = false

            for (i in items.values) {
                if (i.collide(x, y, item.width, item.height)) {
                    conflict = true
                    break
                }
            }
            if (!conflict) {
                items[item.id] = item
                item.inventoryPutTo(this, x, y)

                return true
            }
        }
        return false
    }
}
