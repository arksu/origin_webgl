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
        val gx = player.pos.x / GRID_FULL_SIZE
        val gy = player.pos.y / GRID_FULL_SIZE

        val g = Grid.load(gx, gy, player.pos.level, player.pos.region)
        grids.add(g)
        println(g)

        // находим гриды которые нужны для спавна игрока
        return true
    }
}