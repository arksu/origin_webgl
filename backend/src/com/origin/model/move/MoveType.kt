package com.origin.model.move

enum class MoveType {
    // используется только когда объект спавнится в мир, или телепорт в другое место
    SPAWN,

    // красться
    STEAL,

    // передвижение по суше
    WALK,

    // бегом
    RUN
}