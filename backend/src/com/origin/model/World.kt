package com.origin.model

import com.origin.ObjectID
import java.util.concurrent.ConcurrentHashMap

/**
 * весь игровой мир
 */
object World {
    /**
     * регионы (материки, истансы)
     */
    private val regions = ConcurrentHashMap<Int, Region>()

    /**
     * активные игроки которые залогинены в мир
     */
    private val players = ConcurrentHashMap<ObjectID, Player>()

    fun getRegion(regionId: Int): Region {
        if (regionId < 0) throw RuntimeException("wrong grid region")
        return regions.computeIfAbsent(regionId) {
            val (w, h) = getRegionSize(regionId)
            Region(regionId, w, h)
        }
    }

    /**
     * получить грид по позиции
     */
    fun getGrid(pos: ObjectPosition): Grid {
        return getRegion(pos.region).getLayer(pos.level).getGrid(pos.gridX, pos.gridY)
    }

    /**
     * получить грид по координатам
     */
    fun getGrid(region: Int, level: Int, gx: Int, gy: Int): Grid {
        return getRegion(region).getLayer(level).getGrid(gx, gy)
    }

    fun addPlayer(player: Player) {
        players[player.id] = player
    }

    fun removePlayer(player: Player) {
        players.remove(player.id)
    }

    fun findPlayer(id: ObjectID): Player? {
        return players[id]
    }

    fun playersIterator(): Iterator<Map.Entry<ObjectID, Player>> {
        return players.iterator()
    }

    suspend fun disconnectAllCharacters() {
        players.values.forEach {
            it.socket?.kick()
        }
    }

    fun getPlayersCount(): Int {
        return players.size
    }

    private fun getRegionSize(regionId: Int): Pair<Int, Int> {
        return when (regionId) {
            0 -> Pair(50, 50) // TODO region sizes
            else -> throw RuntimeException("Unknown region $regionId")
        }
    }
}
