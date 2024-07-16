package com.origin.model.action

import com.origin.model.Player
import com.origin.model.`object`.tree.Tree

class ChopTree(
    player: Player,
    val tree: Tree
) : Action(player) {
    override val ticks = 5
    override val staminaConsume = 100
    override val staminaRequired = 0

    override fun run(): Boolean {
        return true
    }
}