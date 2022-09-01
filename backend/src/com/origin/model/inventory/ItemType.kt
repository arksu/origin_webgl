package com.origin.model.inventory

enum class ItemType(val id: Int, val icon: String, val width: Int = 1, val height: Int = 1) {
    STONE(1, "/items/stone.png"),
    APPLE(2, "/items/apple.png"),
    BRANCH(3, "/items/branch.png"),
    RABBIT(4, "/items/rabbit.png", 2, 2),
    BOARD(5, "/items/board.png", 1, 4),
    BARK(6, "/items/bark.png"),
    STONE_AXE(7, "/items/stone_axe.png"),
    BUCKET(8, "/items/bucket.png");

    companion object {
        private val map = values().associateBy(ItemType::id)
        val mapNames = values().associateBy(ItemType::name).mapKeys {
            it.key.lowercase()
        }

        fun fromId(id: Int) = map[id] ?: throw IllegalArgumentException("item type not found $id")
        fun fromName(name: String) = mapNames[name] ?: throw IllegalArgumentException("item type not found $name")
    }
}

data class ItemWithCount(
    val item: ItemType,
    var count: Int = 1
) {
    constructor(i: ItemWithCount) : this(i.item, i.count)
}

class RequiredList(
    list: List<ItemWithCount>
) {
    val left = ArrayList<ItemWithCount>(8)

    init {
        // добавим все вещи из переданного списка, с созданием новых экземпляров
        // т.к. будем изменять количество в них
        list.forEach {
            left.add(ItemWithCount(it))
        }
    }

    /**
     * проверить что указанный тип вещи есть в требуемом списке,
     * удалить его из списка
     */
    fun checkAndDecrement(item: ItemType): Boolean {
        val itr = left.iterator()
        while (itr.hasNext()) {
            val l = itr.next()
            if (l.item == item && l.count >= 1) {
                l.count--
                if (l.count <= 0) itr.remove()
                return true
            }
        }
        return false
    }

    fun isEmpty() = left.isEmpty()
}
