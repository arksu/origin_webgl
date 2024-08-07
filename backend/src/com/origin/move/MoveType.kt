package com.origin.move

/**
 * тип перемещения для вычисления коллизий
 */
enum class MoveType {
    // используется только когда объект спавнится в мир, или телепорт в другое место
    SPAWN,

    // передвижение по суше
    WALK,

    // плывем
    SWIMMING
}
