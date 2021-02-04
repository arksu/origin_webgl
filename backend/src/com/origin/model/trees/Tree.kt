package com.origin.model.trees

import com.origin.entity.EntityObject
import com.origin.model.ContextMenu
import com.origin.model.Player
import com.origin.model.StaticObject
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
        return ContextMenu(this, "chop", "takeBranch")
    }

    override fun processContextItem(item: String) {
        when (item) {
            "chop" -> {

            }
            "takeBranch" -> {

            }
        }
    }
}