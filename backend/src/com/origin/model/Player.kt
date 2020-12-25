package com.origin.model

import com.origin.entity.Character
import com.origin.entity.Grid
import com.origin.net.model.GameSession

/**
 * инстанс персонажа игрока в игровом мире (игрок)
 */
class Player(
    /**
     * персонаж игрока (сущность хранимая в БД)
     */
    private val character: Character,

    private val session: GameSession,
) : Human(character) {

    /**
     * одежда (во что одет игрок)
     */
    val paperdoll: Paperdoll = Paperdoll(this)



}