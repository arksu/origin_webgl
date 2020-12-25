package com.origin.model

import com.origin.entity.Grid
import com.origin.utils.ObjectID
import java.util.concurrent.ConcurrentHashMap

/**
 * весь игровой мир
 */
class World {
    /**
     * регионы (материки, истансы)
     */
    private val regions = ConcurrentHashMap<Int, Region>()

    /**
     * список активных гридов которые надо обновлять
     */
    private val activeGrids = ConcurrentHashMap.newKeySet<Grid>(9)

    private val players = ConcurrentHashMap<ObjectID, Player>()

    private fun getRegion(region: Int): Region {
        if (region < 0) throw RuntimeException("wrong grid region")
        return regions.computeIfAbsent(region) { Region(region) }
    }

    /**
     * получить грид по позиции
     */
    fun getGrid(pos: Position): Grid {
        return getRegion(pos.region).getLayer(pos.level).getGrid(pos.gridX, pos.gridY)
    }

    /**
     * получить грид по координатам
     */
    fun getGrid(region: Int, level: Int, gx: Int, gy: Int): Grid {
        return getRegion(region).getLayer(level).getGrid(gx, gy)
    }

    /**
     * добавить активный грид в список активных (для апдейта)
     */
    fun addActiveGrid(grid: Grid) {
        activeGrids.add(grid)
    }

    /**
     * удалить активный грид (больше не будет обновляться)
     */
    fun removeActiveGrid(grid: Grid) {
        activeGrids.remove(grid)
    }

    companion object {
        @JvmField
        val instance = World()
    }
}