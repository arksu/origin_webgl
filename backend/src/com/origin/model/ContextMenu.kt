package com.origin.model

import com.origin.model.move.Move2Object
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * контекстное меню объекта
 * создается при вызове меню. и сохраняется в объекте игрока пока не будет выбран пункт меню
 * или отмена меню
 */
@ObsoleteCoroutinesApi
class ContextMenu(val obj: GameObject, vararg i: String) {
    val items: List<String> = i.asList()

    suspend fun processItem(player: Player, item: String) {
        if (items.contains(item)) {
            // в любом действии контекстного меню надо идти к объекту
            player.startMove(Move2Object(player, obj) {
                // и потом запустить само действие
                obj.processContextItem(player, item)
            })
        }
    }
}