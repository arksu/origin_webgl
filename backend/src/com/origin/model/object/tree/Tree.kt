package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.ContextMenu
import com.origin.model.Player
import com.origin.model.StaticObject
import com.origin.model.inventory.Inventory
import com.origin.model.inventory.ItemType
import com.origin.util.Rect

abstract class Tree(record: ObjectRecord) : StaticObject(record) {
    /**
     * стадия роста
     * если есть данные объекта то это и есть номер стадии.
     * если нет данных ставим по дефолту 6 стадию роста
     */
    var stage: Int = record.data?.toInt() ?: 6

    override val inventory: Inventory? = null

    override fun getBoundRect(): Rect {
        return Rect(6)
    }

    override fun contextMenu(p: Player): ContextMenu {
        return ContextMenu(this, "Chop", "Take branch", "Take bark")
    }

    override suspend fun processContextItem(player: Player, item: String) {
        logger.debug("processContextItem $player $item")
        when (item) {
//            "Chop" -> {
//                player.startActionCyclic(
//                    this, 3, getMaxHP() - this.entity.hp, getMaxHP(),
//                    Status.reduceStamina(4.0)
//                ) { it, _ ->
//                    var done = false
//                    if (it.target is Tree) {
//                        // не удалось снять очередные хп с дерева
//                        if (!it.target.takeHP(6)) {
//                            // значит хп кончилось. и дерево срубили
//                            logger.warn("TREE CHOPPED!")
//                            // make tree -> stump
//                            transaction {
//                                // меняем тип на "пень" (стадия)
//                                it.target.entity.data = "10"
//                                // дадим ему сколько то хп
//                                it.target.entity.hp = 120
//                            }
//                            it.target.stage = 10
//                            // уведомим окружающие объекты о том что это дерево изменилось
//                            it.target.grid.broadcast(BroadcastEvent.Changed(it.target))
//
//                            // TODO а тут надо заспавнить 2 бревна
//                            done = true
//                        } else {
//                            it.sendPacket(ActionProgress(it.maxProgress - it.target.entity.hp, it.maxProgress))
//                        }
//                    }
//                    done
//                }
//            }
            "Take branch" -> {
                player.startActionOnce(
                    this, 2, 21,
                    // возьмем у игрока часть стамины
//                    Status.reduceStamina(1.0),
                    Action.generateItems(ItemType.BRANCH)
                )
            }
//            "Take bark" -> {
//                player.startActionOnce(
//                    this, 2, 2,
//                    // возьмем у игрока часть стамины
//                    Status.reduceStamina(1.0),
//                    Action.generateItems(ItemType.BARK)
//                )
//            }
        }
    }
}