package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.ContextMenu
import com.origin.model.Player
import com.origin.model.StaticObject
import com.origin.model.kind.Liftable
import com.origin.model.`object`.ObjectsFactory
import com.origin.util.Rect

/**
 * бревно когда срубаем дерево
 */
class WoodenLog(record: ObjectRecord) : StaticObject(record), Liftable {

    companion object {
        init {
            ObjectsFactory.add(14, WoodenLog::class.java)
        }
    }

    override fun getResourcePath(): String {
        return "log"
    }

    override fun getBoundRect(): Rect {
        return Rect.EMPTY
    }

    override fun openContextMenu(p: Player): ContextMenu? {
        val items = ArrayList<String>(4)
        items.add("Make board")

        return ContextMenu(this, items)
    }

    override suspend fun executeContextMenuItem(player: Player, selected: String) {
        // TODO Make board
    }
}