package com.origin.model.inventory

import com.origin.ObjectID
import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.references.INVENTORY
import com.origin.model.GameObject
import com.origin.model.Player
import com.origin.model.item.Item
import com.origin.model.item.ItemFactory
import com.origin.model.`object`.container.Container
import com.origin.net.InventoryUpdate
import java.util.concurrent.ConcurrentHashMap

class Inventory(private val parent: GameObject) {
    val id: ObjectID = parent.id

    val items = ConcurrentHashMap<ObjectID, Item>()

    val title: String
        get() {
            return parent::class.java.simpleName
        }

    init {
        val list = DatabaseConfig.dsl
            .selectFrom(INVENTORY)
            .where(INVENTORY.INVENTORY_ID.eq(id))
            .and(INVENTORY.X.lessThan(200))
            .and(INVENTORY.Y.lessThan(200))
            .and(INVENTORY.DELETED.isFalse)
            .fetch()
        list.forEach {
            val item = ItemFactory.create(it)
            item.inventory = this
            items[it.id] = item
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
     * @param toPlayer какому игроку отправить содержимое этого инвентаря
     */
    suspend fun sendInventory(toPlayer: Player) {
        toPlayer.session.send(InventoryUpdate(this))
    }

    /**
     * уведомить родителя об изменениях в инвентаре
     */
    suspend fun notify() {
        when (parent) {
            is Player -> {
                sendInventory(parent)
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
     * @param isTake надо ли забирать из инвентаря вещи, иначе просто проверяем и возвращаем список того что нашли
     */
    suspend fun findAndTakeItem(list: List<ItemWithCount>, isTake: Boolean = true): List<Item>? {
        val result = ArrayList<Item>(8)
        val req = RequiredList(list)
        // идем по всем вещам в инвентаре
        items.forEach {
            // проверим что такая вещь есть в списке требований
            if (req.checkAndDecrement(it.value.typeId)) {
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
                val removedItem = items.remove(it.id)
                removedItem?.onInventoryRemove()
            }

            notify()
        }

        // вернем список вещей которые взяли и удалили из инвентаря
        return result
    }

    suspend fun takeItem(id: ObjectID): Item? {
        val removed = items.remove(id)
        if (removed != null) {
            removed.onInventoryRemove()
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
    suspend fun putItem(item: Item, x: Int, y: Int): Boolean {
        val result = tryPut(item, x, y)
        if (result) {
            notify()
        }
        return result
    }

    /**
     * положить вещь в инвентарь
     * попытаться положить в любое место которое свободно
     */
    suspend fun putItem(item: Item): Boolean {
        // перебираем все возможные координаты в инвентаре
        for (iy in 0 until getHeight()) for (ix in 0 until getWidth())
        // пробуем положить вещь в эти координаты
            if (tryPut(item, ix, iy)) {
                notify()
                return true
            }
        return false
    }

    /**
     * заспавнить вещь в инвентарь, если не влезает кинуть рядом на землю
     */
    suspend fun spawnItem(item: Item, dropIfNoSpace: Boolean = false): Boolean {
        if (!putItem(item)) {
            return if (dropIfNoSpace) {
                // TODO drop down
                true
            } else false
        }
        return true
    }

    /**
     * проверка можно ли положить вещь в этот инвентарь
     */
    private fun tryPut(item: Item, x: Int, y: Int): Boolean {
        // проверяем что вещь влезает вообще в инвентарь по размеру
        if (x >= 0 && y >= 0 && x + item.width <= getWidth() && y + item.height <= getHeight()) {
            var conflict = false

            // проверим на коллизии со всеми вещами которые лежат в инвентаре
            for (i in items.values) {
                if (i.collide(x, y, item.width, item.height)) {
                    conflict = true
                    break
                }
            }
            if (!conflict) {
                items[item.id] = item
                item.onInventoryPut(this, x, y)

                return true
            }
        }
        return false
    }
}