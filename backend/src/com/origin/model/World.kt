package com.origin.model

import com.origin.utils.MapUtils
import java.util.concurrent.ConcurrentHashMap

/**
 * весь игровой мир
 */
class World {
    private val _regions = ConcurrentHashMap<Int, Region>()

    /**
     * добавить игрока в мир
     * @return получилось ли добавить (заспавнить) игрока в мир
     */
    fun spawnPlayer(player: Player): Boolean {
        // создаем грид в котором находится игрок
        val region = _regions.computeIfAbsent(player.region) { Region(player.region) }

        // сам инстанс уже спавнит игрока
        return region.spawnPlayer(player)
    }

    companion object {
        @JvmField
        val instance = World()
    }
}