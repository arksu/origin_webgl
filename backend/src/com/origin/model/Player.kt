package com.origin.model

import com.origin.entity.Character
import com.origin.entity.Grid
import com.origin.net.model.GameSession

/**
 * инстанс персонажа игрока в игровом мире (игрок)
 */
class Player : GameObject {
    constructor(character: Character, session: GameSession) : super(character.x,
        character.y,
        character.level,
        character.region,
        character.heading) {

        this.character = character
        this.session = session

        paperdoll = Paperdoll()
    }

    /**
     * персонаж игрока (сущность хранимая в БД)
     */
    val character: Character

    private val session: GameSession

    /**
     * одежда (во что одет игрок)
     */
    val paperdoll: Paperdoll


    /**
     * текущий активный грид игрока
     */
    private val grid: Grid? = null

}