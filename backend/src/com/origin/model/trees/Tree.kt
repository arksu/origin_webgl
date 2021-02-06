package com.origin.model.trees

import com.origin.entity.EntityObject
import com.origin.model.ContextMenu
import com.origin.model.Player
import com.origin.model.StaticObject
import com.origin.model.move.Move2Object
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
                player.startMove(Move2Object(player, this) {
                    logger.debug("tree !")
                    player.startAction(this, 3, {
                        // возьмем у игрока часть стамины и голода
                        it.stamina.take(4)
                    }) {
                        logger.debug("action tick")
                        var done = false
                        if (it.target is Tree) {
                            // не удалось снять очередные хп с дерева
                            if (!it.target.takeHp(20)) {
                                // значит хп кончилось. и дерево срубили
                                logger.warn("CHOP")
                                done = true
                            }
                        }
                        done
                    }
                })
            }
            "Take branch" -> {

            }
            "Take bark" -> {

            }
        }
    }
}