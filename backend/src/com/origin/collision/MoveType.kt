package com.origin.collision

enum class MoveType {
    // используется только когда объект спавнится в мир, или телепорт в другое место
    SPAWN,

    // передвижение по суше
    WALK,

    // бегом
    RUN,

    // плывет по воде
    SWIMMING
}