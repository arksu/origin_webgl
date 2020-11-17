package com.origin.model

import java.util.concurrent.ConcurrentHashMap

/**
 * весь игровой мир
 */
class World {
    private val _instances = ConcurrentHashMap<Int, Area>()

    /**
     * добавить игрока в мир
     * @return получилось ли добавить (заспавнить) игрока в мир
     */
    fun spawnPlayer(player: Player): Boolean {
        // создаем грид в котором находится игрок
        val instance = _instances.computeIfAbsent(player.instanceId) { Area(player.instanceId) }

        // сам инстанс уже спавнит игрока
        return instance.spawnPlayer(player)
    }

    companion object {
        @JvmField
        val instance = World()
    }
}