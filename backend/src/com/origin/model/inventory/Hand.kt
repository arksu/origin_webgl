package com.origin.model.inventory

import com.origin.model.Player
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * рука в которой игрок держит вещь (inventory item)
 */
@ObsoleteCoroutinesApi
class Hand(
    private val me: Player,
    val item: InventoryItem,

    /**
     * отступ в координатах инвентаря (чтобы положить обратно корректно)
     */
    private val offsetX: Int,
    private val offsetY: Int,

    val mouseX: Int,
    val mouseY: Int,
) {
}