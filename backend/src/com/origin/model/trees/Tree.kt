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
                player.startMove(Move2Object(player, this))
            }
            "Take branch" -> {

            }
            "Take bark" -> {

            }
        }
    }
}