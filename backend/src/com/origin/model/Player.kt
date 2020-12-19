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
    private val session: GameSession,
) : GameObject() {
    /**
     * одежда (во что одет игрок)
     */
    var paperdoll: Paperdoll? = null

    /**
     * координаты кэшируем в объекте (потом периодически обновляем в сущности Character)
     */
    override var x: Int = character.x
        set(value) {
            field = value
            // TODO
            println(value)
        }
    override var y: Int = character.y
        set(value) {
            field = value
            // TODO
            println(value)
        }
    override var level: Int = character.level
    override var region: Int = character.region

    /**
     * текущий активный грид игрока
     */
    private val grid: Grid? = null

}