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
    val character: Character,
    private val _session: GameSession
) : GameObject() {
    /**
     * одежда (во что одет игрок)
     */
    var paperdoll: Paperdoll? = null

    /**
     * координаты кэшируем в объекте (потом периодически обновляем в сущности Character)
     */
    override val x: Int = character.x
    override val y: Int = character.y
    override val level: Int = character.level
    override val region: Int = character.region

    /**
     * текущий активный грид игрока
     */
    private val _grid: Grid? = null

}