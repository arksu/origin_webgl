package com.origin.model

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
            obj.processContextItem(player, item)
        }
    }
}