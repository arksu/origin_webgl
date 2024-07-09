package com.origin.model.inventory

import com.origin.model.Player

/**
 * рука в которой игрок держит вещь (inventory item)
 */
class Hand(
    private val me: Player,

    val item: InventoryItem,

    /**
     * отступ в координатах инвентаря (чтобы положить обратно корректно)
     */
    val offsetX: Int,
    val offsetY: Int,

    val mouseX: Int,
    val mouseY: Int,
)
