package com.origin.model.inventory

import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.references.INVENTORY
import com.origin.model.Player
import com.origin.model.item.Item
import com.origin.model.item.ItemFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
        val logger: Logger = LoggerFactory.getLogger(Hand::class.java)

        fun load(player: Player): Hand? {
            val list = DatabaseConfig.dsl
                .selectFrom(INVENTORY)
                .where(INVENTORY.INVENTORY_ID.eq(player.id))
                .and(INVENTORY.X.eq(1000))
                .and(INVENTORY.Y.eq(1000))
                .orderBy(INVENTORY.ID.desc())
                .fetch()
            if (list.size > 1) {
                logger.warn("player $player have multiple hands ${list.size}")
            }
            val rec = list.firstOrNull()
            if (rec != null) {
                val item = ItemFactory.create(rec)
                return Hand(player, item, 0, 0, 15, 15)
            } else return null
        }
    }
}
