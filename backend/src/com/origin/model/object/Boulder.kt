package com.origin.model.`object`

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.ContextMenu
import com.origin.model.Player
import com.origin.model.StaticObject
import com.origin.model.action.ChipStone
import com.origin.util.Rect
import com.origin.util.Rnd

class Boulder(record: ObjectRecord) : StaticObject(record) {
    companion object {
        init {
            ObjectsFactory.add(13, Boulder::class.java)
        }
    }

    override fun getResourcePath(): String {
        return "boulder"
    }

    override fun getBoundRect(): Rect {
        return Rect(6)
    }

    var stone = record.data?.toInt() ?: (Rnd.next(15) + 61)

    override fun openContextMenu(p: Player): ContextMenu? {
        val items = LinkedHashSet<String>(4)
        if (stone > 0) {
            items.add("Chip stone")
        }

        return if (items.isNotEmpty()) ContextMenu(this, items) else null
    }

    override suspend fun executeContextMenuItem(player: Player, selected: String) {
        when (selected) {
            "Chip stone" -> if (stone > 0) {
                player.action = ChipStone(player, this)
            }
        }
    }

    override fun saveData() {
        record.data = stone.toString()
        super.saveData()
    }
}