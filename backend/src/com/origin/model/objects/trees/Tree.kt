package com.origin.model.objects.trees

import com.origin.entity.EntityObject
import com.origin.model.*
import com.origin.model.inventory.Inventory
import com.origin.model.inventory.ItemType
import com.origin.net.model.ActionProgress
import com.origin.utils.Rect
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Деревья
 */
@DelicateCoroutinesApi
@ObsoleteCoroutinesApi
abstract class Tree(entity: EntityObject) : StaticObject(entity) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Tree::class.java)
    }

    /**
     * стадия роста
     * если есть данные объекта то это и есть номер стадии.
     * если нет данных ставим по дефолту 6 стадию роста
     */
    var stage: Int = entity.data?.toInt() ?: 6

    override val inventory: Inventory? = null

    override fun contextMenu(p: Player): ContextMenu {
        return ContextMenu(this, "Chop", "Take branch", "Take bark")
    }

    override fun getBoundRect(): Rect {
        return Rect(6)
    }

    override suspend fun processContextItem(player: Player, item: String) {
        logger.debug("processContextItem $player $item")
        when (item) {
            "Chop" -> {
                player.startActionCyclic(
                    this, 3, getMaxHP() - this.entity.hp, getMaxHP(),
                    Status.reduceStamina(4.0)
                ) { it, _ ->
                    var done = false
                    if (it.target is Tree) {
                        // не удалось снять очередные хп с дерева
                        if (!it.target.takeHP(6)) {
                            // значит хп кончилось. и дерево срубили
                            logger.warn("TREE CHOPPED!")
                            // make tree -> stump
                            transaction {
                                // меняем тип на "пень" (стадия)
                                it.target.entity.data = "10"
                                // дадим ему сколько то хп
                                it.target.entity.hp = 120
                            }
                            it.target.stage = 10
                            // уведомим окружающие объекты о том что это дерево изменилось
                            it.target.grid.broadcast(BroadcastEvent.Changed(it.target))

                            // TODO а тут надо заспавнить 2 бревна
                            done = true
                        } else {
                            it.sendPacket(ActionProgress(it.maxProgress - it.target.entity.hp, it.maxProgress))
                        }
                    }
                    done
                }
            }
            "Take branch" -> {
                player.startActionOnce(
                    this, 2, 21,
                    // возьмем у игрока часть стамины
                    Status.reduceStamina(1.0),
                    Action.generateItems(ItemType.BRANCH)
                )
            }
            "Take bark" -> {
                player.startActionOnce(
                    this, 2, 2,
                    // возьмем у игрока часть стамины
                    Status.reduceStamina(1.0),
                    Action.generateItems(ItemType.BARK)
                )
            }
        }
    }
}
