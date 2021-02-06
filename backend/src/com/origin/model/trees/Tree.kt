package com.origin.model.trees

import com.origin.entity.EntityObject
import com.origin.model.ContextMenu
import com.origin.model.Player
import com.origin.model.StaticObject
import com.origin.net.model.ActionProgress
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Деревья
 */
@ObsoleteCoroutinesApi
open class Tree(entity: EntityObject) : StaticObject(entity) {
    /**
     * стадия роста
     * если есть данные объекта то это и есть номер стадии.
     * если нет данных ставим по дефолту 6 стадию роста
     */
    var stage: Int = entity.data?.toInt() ?: 6

    override fun contextMenu(p: Player): ContextMenu {
        return ContextMenu(this, "Chop", "Take branch", "Take bark")
    }

    override suspend fun processContextItem(player: Player, item: String) {
        when (item) {
            "Chop" -> {
                player.startAction(this, 2, getMaxHP() - this.entity.hp, getMaxHP(), {
                    // возьмем у игрока часть стамины и голода
                    it.stamina.take(2)
                }) {
                    var done = false
                    if (it.target is Tree) {
                        // не удалось снять очередные хп с дерева
                        if (!it.target.takeHP(6)) {
                            // значит хп кончилось. и дерево срубили
                            logger.warn("TREE CHOPPED!")
                            // TODO make tree -> stump
                            done = true
                        } else {
                            it.sendPkt(ActionProgress(it.maxProgress - it.target.entity.hp, it.maxProgress))
                        }
                    }
                    done
                }
            }
            "Take branch" -> {
                player.startAction(this, -4, 0, 21, {
                    // возьмем у игрока часть стамины
                    it.stamina.take(1)
                }) {
                    // TODO generate branch to players inventory
                    logger.warn("GEN BRANCH")
                    true
                }
            }
            "Take bark" -> {
                player.startAction(this, 4, 0, 10, {
                    // возьмем у игрока часть стамины
                    it.stamina.take(1)
                }) {
                    // TODO generate bark to players inventory
                    true
                }
            }
        }
    }
}