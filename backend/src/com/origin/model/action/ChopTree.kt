package com.origin.model.action

import com.origin.model.BroadcastEvent
import com.origin.model.GridMessage
import com.origin.model.Player
import com.origin.model.`object`.ObjectsFactory
import com.origin.model.`object`.tree.Tree
import com.origin.move.PositionModel
import kotlin.math.min

class ChopTree(
    player: Player,
    val tree: Tree
) : Action(player) {
    override val ticks = 12
    override val staminaConsume = 1
    override val minimumStaminaRequired = 300

    private val chop = 100

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

            // спавним бревна
            for (l in 1..tree.logs) {
                val pos = PositionModel(tree.pos.x, tree.pos.y - 20 * l - 3, tree.pos)
                val record = ObjectsFactory.createAndInsert(14, pos)
                val logObject = ObjectsFactory.constructByRecord(record)

                logObject.setGrid(tree.getGridSafety())
                // шлем сообщение самому себе на спавн объекта
                // т.к. мы сейчас в корутине
                tree.getGridSafety().send(GridMessage.SpawnForce(logObject))
            }

            // действие завершается
            true
        } else false // иначе продолжаем рубить
    }

    override fun getProgress(): Pair<Int, Int> {
        return Pair(min(tree.chop + ((tick * chop) / ticks), tree.chopThreshold), tree.chopThreshold)
    }
}