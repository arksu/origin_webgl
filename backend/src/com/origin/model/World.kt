package com.origin.model

import com.origin.entity.Grid
import java.util.concurrent.ConcurrentHashMap

/**
 * весь игровой мир
 */
class World {
    private val regions = ConcurrentHashMap<Int, Region>()

    private fun getRegion(region: Int): Region {
        if (region < 0) throw RuntimeException("wrong grid region")
        return regions.computeIfAbsent(region) { Region(region) }
    }

    fun getGrid(pos: Position): Grid {
        return getRegion(pos.region).getLayer(pos.level).getGrid(pos.gridX, pos.gridY)
    }

    fun getGrid(region: Int, level: Int, gx: Int, gy: Int): Grid {
        return getRegion(region).getLayer(level).getGrid(gx, gy)
    }

    companion object {
        @JvmField
        val instance = World()
    }
}