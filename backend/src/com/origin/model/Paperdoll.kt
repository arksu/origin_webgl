package com.origin.model

import com.origin.entity.InventoryItemEntity
import com.origin.entity.InventoryItems
import com.origin.model.inventory.InventoryItem
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * кукла персонажа, что одето на персе
 */
@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
class Paperdoll(val player: Player) {

    /**
     * храним вещи одетые на сущность в массиве
     * доступ к элементам по индексу {@see Paperdoll.Slot}
     */
    private val items: MutableList<InventoryItem> = ArrayList(16)

    enum class Slot(val id: Int) {
        LEFT_HAND(1), RIGHT_HAND(2), HEAD(3), EYES(4), BODY(5), LEGS(6);
    }

    init {
        // загрузка из базы
        transaction {
            // все слоты с x >= 200 и y = 0 считаем вещами из папердолла. слот в папердолле это x-200
            InventoryItemEntity
                .find { (InventoryItems.inventoryId eq player.id) and (InventoryItems.x greaterEq 200) and (InventoryItems.y eq 0) }
                .forEach {
                    items[it.x - 200] = InventoryItem(it, null)
                }
        }
    }
}
