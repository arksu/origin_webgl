package com.origin.model

import java.util.concurrent.ConcurrentHashMap

/**
 * игровая область (континент, материк)
 * в игре может быть несколько больших континентов одновременно
 */
class Region(private val id: Int) {
    private val layers = ConcurrentHashMap<Int, LandLayer>()

    fun spawnPlayer(player: Player): Boolean {
        val layer = layers.computeIfAbsent(player.pos.region) { LandLayer(this, player.pos.level) }

        // сам уровень земли уже спавнит игрока
        return layer.spawnPlayer(player)
    }
}