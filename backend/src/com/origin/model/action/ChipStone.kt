package com.origin.model.action

import com.origin.model.Player
import com.origin.model.item.ItemFactory
import com.origin.model.item.Stone
import com.origin.model.`object`.Boulder

class ChipStone(val player: Player, private val boulder: Boulder) : Action(player) {
    override val ticks = 16
    override val staminaConsume = 50
    override val minimumStaminaRequired = 0

    override suspend fun run(): Boolean {
        if (boulder.stone > 0) {
            val branch = ItemFactory.create(Stone::class.java, quality = boulder.quality)
            val success = player.inventory.spawnItem(branch)
            if (success) {
                boulder.stone--
                boulder.saveData()
            }
            // finished ?
            return !success || boulder.stone == 0
        }
        return true
    }
}