package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.ContextMenu
import com.origin.model.Player
import com.origin.model.`object`.StaticObject
import com.origin.model.action.ChopTree
import com.origin.model.action.TakeBranch
import com.origin.util.Rect

abstract class Tree(record: ObjectRecord) : StaticObject(record) {

    /**
     * стадия роста
     * если есть данные объекта то это и есть номер стадии.
     * если нет данных ставим по дефолту 6 стадию роста
     */
    var stage: Int
        get() = entity.data?.toInt() ?: 6
        set(s) {
            entity.data = s.toString()
        }

    /**
     * текущее число очков рубки когда рубим дерево
     */
    var chop: Int = 0

    /**
     * сколько веток осталось на дереве
     */
    var branch = 0

    /**
     * сколько коры осталось на дереве
     */
    var bark = 0

    /**
     * сучья (лапник, с листьями)
     */
    var bough = 0

    /**
     * сколько очков рубки надо потратить чтобы целиком срубить дерево
     */
    open val chopThreshold: Int = 1000

    open val maxBranch = 5
    open val maxBark = 3
    open val logs = 2

    override fun postConstruct() {
        bark = maxBark
        branch = maxBranch
    }

    override fun getBoundRect(): Rect {
        return Rect(6)
    }

    override fun openContextMenu(p: Player): ContextMenu? {
        // TODO смотрим на скиллы, одет ли топор,
        //  остались ветки? сучья? кора?
        val items = LinkedHashSet<String>(4)

        // TODO if Axe
        items.add("Chop")

        if (branch > 0) {
            items.add("Take branch")
        }
        if (bark > 0) {
            items.add("Take bark")
        }
        if (bough > 0) {
            items.add("Take bough")
        }
        return if (items.isNotEmpty()) ContextMenu(this, items) else null
    }

    override suspend fun executeContextMenuItem(player: Player, selected: String) {
        logger.debug("Tree context menu $player - $selected")
        when (selected) {
            "Chop" -> {
                player.action = ChopTree(player, this)
            }

            "Take branch" -> if (branch > 0) {
                player.action = TakeBranch(player, this)
            }

            "Take bark" -> if (bark > 0) {
            }

            "Take bough" -> if (bough > 0) {

            }
        }
    }
}