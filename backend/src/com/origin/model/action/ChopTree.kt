package com.origin.model.action

import com.origin.model.*
import com.origin.model.`object`.tree.Tree
import com.origin.model.`object`.tree.WoodenLog
import kotlin.math.abs
import kotlin.math.min

class ChopTree(
    player: Player,
    val tree: Tree
) : Action(player) {
    override val ticks = 10
    override val staminaConsume = 10
    override val minimumStaminaRequired = 300
    override val tickDelay = 120L

    // сколько сносим дереву за 1 цикл действия
    private val chop = 50

    override fun condition(): Boolean {
        // рубим до тех пор, пока очки рубки не превышают максимум
        return tree.chop < tree.chopThreshold && super.condition()
    }

    override suspend fun run(): Boolean {
        // добавляем очки рубки дереву
        tree.chop += chop
        logger.debug("chop tree ${tree.chop}")
        // если целиком срубили дерево
        return if (tree.chop >= tree.chopThreshold) {
            // теперь это пень
            tree.stage = 10
            tree.saveData()
            // уведомим окружающие объекты о том что это дерево изменилось
            tree.getGridSafety().broadcast(BroadcastEvent.Changed(tree))

            // определим позицию игрока относительно дерева
            // чтобы повалить дерево в нужном направлении
            val delta = tree.pos.point.sub(me.pos.point)
            var multY = 0
            var multX = 0
            if (abs(delta.x) > abs(delta.y)) {
                multX = if (delta.x < 0) {
                    -1
                } else {
                    1
                }
            } else {
                multY = if (delta.y < 0) {
                    -1
                } else {
                    1
                }
            }

            // спавним бревна
            for (l in 1..tree.logs) {
                val pos = ObjectPosition(tree.pos.x + (20 * l + 3) * multX, tree.pos.y + (20 * l + 3) * multY, tree.pos)
                val g = World.getGrid(pos)
                Grid.generateObject(g, WoodenLog::class.java, pos)
            }

            // действие завершается
            true
        } else false // иначе продолжаем рубить
    }

    override fun getProgress(): Pair<Int, Int> {
        return Pair(min(tree.chop + ((tick * chop) / ticks), tree.chopThreshold), tree.chopThreshold)
    }
}