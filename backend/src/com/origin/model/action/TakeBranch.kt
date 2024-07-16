package com.origin.model.action

import com.origin.model.Player
import com.origin.model.`object`.tree.Tree

class TakeBranch(player: Player, tree: Tree) : Action(player) {
    override val ticks = 10
    override val staminaConsume = 50
    override val staminaRequired = 0

    override fun run(): Boolean {
        return false
    }
}