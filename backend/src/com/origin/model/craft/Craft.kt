package com.origin.model.craft

import com.origin.model.Skill
import com.origin.model.item.Item
import com.origin.model.item.ItemFactory

class Craft(
    val name: String,
    val ticks: Int,
    val staminaConsume: Int,
    val minimumStaminaRequired: Int = 0,
    val produce: Map<Class<*>, Int>,
    val requiredItems: Map<Class<*>, Int>,
    val requiredSkills: Collection<Skill>
) {
    fun calcQuality(items: Collection<Item>): Short {
        // TODO: рассчитаем качество создаваемой вещи
        
        return 10
    }
}

data class ItemWithCount(
    val typeId: Int,
    var count: Int = 1
) {
    constructor(i: ItemWithCount) : this(i.typeId, i.count)
}

/**
 * Временный объект для вычисления необходимых вещей для крафта из инвентаря
 */
class RequiredList(
    craft: Craft
) {
    val left = ArrayList<ItemWithCount>(8)

    init {
        // добавим все вещи из переданного списка, с созданием новых экземпляров
        // т.к. будем изменять количество в них
        craft.requiredItems.forEach { (clazz, count) ->
            left.add(ItemWithCount(ItemFactory.getTypeByClass(clazz), count))
        }
    }

    /**
     * проверить что указанный тип вещи есть в требуемом списке,
     * удалить его из списка
     */
    fun checkAndDecrement(typeId: Int): Boolean {
        val itr = left.iterator()
        while (itr.hasNext()) {
            val l = itr.next()
            if (l.typeId == typeId && l.count >= 1) {
                l.count--
                if (l.count <= 0) itr.remove()
                return true
            }
        }
        return false
    }

    fun isEmpty() = left.isEmpty()
}