package com.origin.model.inventory

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