package com.origin.model

import com.origin.jooq.tables.records.CharacterRecord
import com.origin.net.GameSession

class Player(
    /**
     * персонаж игрока (сущность хранимая в БД)
     */
    private val character: CharacterRecord, val session: GameSession
) : GameObject(
    character.id, ObjectPosition(
        initX = character.x,
        initY = character.y,
        level = character.level,
        region = character.region,
        heading = character.heading
    )
) {


}