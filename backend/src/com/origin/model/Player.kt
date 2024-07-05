package com.origin.model

import com.origin.jooq.tables.records.CharacterRecord
import com.origin.net.GameSession
import com.origin.util.PLAYER_RECT
import com.origin.util.Rect

class Player(
    /**
     * персонаж игрока (сущность хранимая в БД)
     */
    private val character: CharacterRecord, val session: GameSession
) : Human(
    character.id, ObjectPosition(
        initX = character.x,
        initY = character.y,
        level = character.level,
        region = character.region,
        heading = character.heading
    )
) {

    override fun getBoundRect(): Rect {
        return PLAYER_RECT
    }

}