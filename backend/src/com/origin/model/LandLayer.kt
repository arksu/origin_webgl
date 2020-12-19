package com.origin.model

import com.origin.entity.Grid
import com.origin.utils.GRID_FULL_SIZE

/**
 * слой (уровень) земли
 */
class LandLayer(
    private val region: Region,

    /**
     * уровень земли
     */
    private val level: Int,
) {
    /**
     * гриды
     */
    var grids: MutableList<Grid> = ArrayList()

    fun spawnPlayer(player: Player): Boolean {
        val gx = player.x / GRID_FULL_SIZE
        val gy = player.y / GRID_FULL_SIZE

        val g = Grid.load(gx, gy, player.level, player.region)
        grids.add(g)
        println(g)

        // находим гриды которые нужны для спавна игрока
        return true
    }
}