package com.origin.model.inventory

import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.references.INVENTORY
import com.origin.model.Player
import com.origin.model.item.Item
import com.origin.model.item.ItemFactory

/**
 * рука в которой игрок держит вещь (inventory item)
 */
class Hand(
    private val me: Player,

    val item: Item,

    /**
     * отступ в координатах инвентаря (чтобы положить обратно корректно)
     */
    val offsetX: Int,
    val offsetY: Int,

    val mouseX: Int,
    val mouseY: Int,
) {
    init {
        item.setXY(1000, 1000)
    }

    companion object {
        fun load(player: Player): Hand? {
            val rec = DatabaseConfig.dsl
                .selectFrom(INVENTORY)
                .where(INVENTORY.INVENTORY_ID.eq(player.id))
                .and(INVENTORY.X.eq(1000))
                .and(INVENTORY.Y.eq(1000))
                .fetchOne()
            if (rec != null) {
                val item = ItemFactory.create(rec)
                return Hand(player, item, 0, 0, 15, 15)
            } else return null
        }
    }
}
