package com.origin.model

import java.util.concurrent.ConcurrentHashMap

/**
 * игровая область (континент, материк)
 * в игре может быть несколько больших континентов одновременно
 */
class Area(private val _id: Int) {
    private val _layers = ConcurrentHashMap<Int, LandLayer>()

    fun spawnPlayer(player: Player): Boolean {
        val layer = _layers.computeIfAbsent(player.instanceId) { LandLayer(this, player.level) }

        // сам уровень земли уже спавнит игрока
        return layer.spawnPlayer(player)
    }
}