package com.origin.model

/**
 * контекстное меню объекта
 * создается при вызове меню. и сохраняется в объекте игрока пока не будет выбран пункт меню
 * или отмена меню
 */
class ContextMenu(vararg i: String) {
    val items = listOf(i)
}