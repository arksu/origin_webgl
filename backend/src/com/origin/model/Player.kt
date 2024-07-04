package com.origin.model

import com.origin.net.GameSession
import com.origin.jooq.tables.records.CharacterRecord

class Player : GameObject {
    /**
     * персонаж игрока (сущность хранимая в БД)
     */
    private val character: CharacterRecord
    val session: GameSession

    constructor(character: CharacterRecord, session: GameSession) : super(
        character.id, ObjectPosition(
            initX = character.x,
            initY = character.y,
            level = character.level,
            region = character.region,
            heading = character.heading
        )
    ) {
        this.character = character
        this.session = session
    }

}