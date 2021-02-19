package com.origin.model.items

import com.origin.entity.InventoryItemEntity
import com.origin.entity.InventoryItems
import com.origin.model.GameObject
import com.origin.model.Player
import com.origin.net.model.InventoryUpdate
import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

@ObsoleteCoroutinesApi
class Inventory(private val parent: GameObject) {

    val inventoryId: ObjectID = parent.id

    val items = ConcurrentHashMap<ObjectID, InventoryItem>()

    val title: String
        get() {
            return parent::class.java.simpleName
        }

    fun getWidth(): Int {
        return 6
    }

    fun getHeight(): Int {
        return 4
    }

    init {
        transaction {
            val list = InventoryItemEntity.find { InventoryItems.inventoryId eq inventoryId }
            list.forEach {
                items[it.id.value] = ItemsFactory.byEntity(this@Inventory, it)
            }
        }
    }

    suspend fun send(player: Player) {
        player.session.send(InventoryUpdate(this))
    }
}