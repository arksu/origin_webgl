package com.origin.model.action

import com.origin.model.Player
import com.origin.model.`object`.tree.Tree

class TakeBranch(player: Player, tree: Tree) : Action(player) {
    override val ticks = 7
    override val staminaConsume = 50
    override val minimumStaminaRequired = 0

    override suspend fun run(): Boolean {
        // если еще есть ветки в дереве И есть место в инвентаре - продолжаем. false
        return false
    }
}