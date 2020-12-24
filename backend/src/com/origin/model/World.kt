package com.origin.model

import java.util.concurrent.ConcurrentHashMap

/**
 * весь игровой мир
 */
class World {
    private val regions = ConcurrentHashMap<Int, Region>()

    /**
     * добавить игрока в мир
     * @return получилось ли добавить (заспавнить) игрока в мир
     */
    fun spawnPlayer(player: Player): Boolean {
        // создаем регион в котором находится игрок
        val region = regions.computeIfAbsent(player.pos.region) { Region(player.pos.region) }

        // сам регион уже спавнит игрока
        return region.spawnPlayer(player)
    }

    companion object {
        @JvmField
        val instance = World()
    }
}