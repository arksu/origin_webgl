package com.origin.model.craft

import com.origin.model.Skill

class Craft(
    val name: String,
    val icon: String,
    val produce: Map<Class<*>, Int>,
    val requiredItems: Map<Class<*>, Int>,
    val requiredSkills: Collection<Skill>
)

data class ItemWithCount(
    val itemTypeId: Int,
    var count: Int = 1
) {
    constructor(i: ItemWithCount) : this(i.itemTypeId, i.count)
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
    fun checkAndDecrement(itemTypeId: Int): Boolean {
        val itr = left.iterator()
        while (itr.hasNext()) {
            val l = itr.next()
            if (l.itemTypeId == itemTypeId && l.count >= 1) {
                l.count--
                if (l.count <= 0) itr.remove()
                return true
            }
        }
        return false
    }

    fun isEmpty() = left.isEmpty()
}