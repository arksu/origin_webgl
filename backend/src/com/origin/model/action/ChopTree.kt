package com.origin.model.action

import com.origin.model.BroadcastEvent
import com.origin.model.Player
import com.origin.model.`object`.tree.Tree
import kotlin.math.min

class ChopTree(
    player: Player,
    val tree: Tree
) : Action(player) {
    override val ticks = 10
    override val staminaConsume = 1
    override val minimumStaminaRequired = 300

    private val chop = 200

    override fun condition(): Boolean {
        // рубим до тех пор, пока очки рубки не превышают максимум
        return tree.chop < tree.chopPoints && super.condition()
    }

    override suspend fun run(): Boolean {
        // добавляем очки рубки дереву
        tree.chop += chop
        logger.debug("chop tree ${tree.chop}")
        // если целиком срубили дерево
        return if (tree.chop >= tree.chopPoints) {
            // теперь это пень
            tree.stage = 10
            tree.saveData()
            // уведомим окружающие объекты о том что это дерево изменилось
            tree.getGridSafety().broadcast(BroadcastEvent.Changed(tree))
            // действие завершается
            true
        } else false // иначе продолжаем рубить
    }

    override fun getProgress(): Pair<Int, Int> {
        return Pair(min(tree.chop + ((tick * chop) / ticks), tree.chopPoints), tree.chopPoints)
    }
}