package com.origin.model

import com.origin.model.item.Item
import com.origin.model.`object`.GameObject
import com.origin.move.Move2Object

/**
 * контекстное меню объекта
 * создается при вызове меню. и сохраняется в объекте игрока пока не будет выбран пункт меню
 * или отмена меню
 */
class ContextMenu {

    val item: Item?
    val obj: GameObject?
    val items: Collection<String>

    constructor(obj: GameObject, items: Collection<String>) {
        this.obj = obj
        this.items = items
        this.item = null
    }

    constructor(item: Item, items: Collection<String>) {
        this.item = item
        this.items = items
        this.obj = null
    }

    suspend fun execute(player: Player, selected: String) {
        if (items.contains(selected)) {
            if (obj != null) {
                // в любом действии контекстного меню надо идти к объекту
                player.startMove(
                    Move2Object(player, obj) {
                        // и потом запустить само действие
                        obj.executeContextMenuItem(player, selected)
                    }
                )
            } else if (item != null) {
                item.executeContextMenuItem(player, selected)
            }
        }
    }
}
