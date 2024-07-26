package com.origin.model.action

import com.origin.model.Player
import com.origin.model.item.Branch
import com.origin.model.item.ItemFactory
import com.origin.model.`object`.tree.Tree

class TakeBranch(val player: Player, val tree: Tree) : Action(player) {
    override val ticks = 7
    override val staminaConsume = 50
    override val minimumStaminaRequired = 0

    override suspend fun run(): Boolean {
        // если еще есть ветки в дереве И есть место в инвентаре - продолжаем.
        if (tree.branch > 0) {
            val branch = ItemFactory.create(Branch::class.java, quality = tree.quality)
            val success = player.inventory.spawnItem(branch)
            if (success) {
                tree.branch--
            }
            // finished ?
            return !success
        }
        return true
    }
}