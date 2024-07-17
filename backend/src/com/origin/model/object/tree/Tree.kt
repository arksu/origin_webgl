package com.origin.model.`object`.tree

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.model.ContextMenu
import com.origin.model.Player
import com.origin.model.StaticObject
import com.origin.model.action.ChopTree
import com.origin.model.action.TakeBranch
import com.origin.model.inventory.Inventory
import com.origin.util.Rect

abstract class Tree(record: ObjectRecord) : StaticObject(record) {
    override val inventory: Inventory? = null

    /**
     * стадия роста
     * если есть данные объекта то это и есть номер стадии.
     * если нет данных ставим по дефолту 6 стадию роста
     */
    var stage: Int
        get() = record.data?.toInt() ?: 6
        set(s) {
            record.data = s.toString()
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

    override fun afterLoad() {
        bark = maxBark
        branch = maxBranch
    }

    override fun getBoundRect(): Rect {
        return Rect(6)
    }

    override fun openContextMenu(p: Player): ContextMenu? {
        // TODO смотрим на скиллы, одет ли топор,
        //  остались ветки? сучья? кора?
        val items = ArrayList<String>(4)
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
        return ContextMenu(this, items)
    }

    override suspend fun executeContextMenuItem(player: Player, item: String) {
        logger.debug("Tree context menu $player - $item")
        when (item) {
            "Chop" -> {
                player.action = ChopTree(player, this)
            }

            "Take branch" -> {
                player.action = TakeBranch(player, this)
            }

            "Take bark" -> {
            }
        }
    }
}