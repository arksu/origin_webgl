package com.origin.model

import com.origin.entity.Grid

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
        val g = Grid.load(player.pos.gridX, player.pos.gridY, player.pos.level, player.pos.region)
        grids.add(g)
        println(g)

        // находим гриды которые нужны для спавна игрока
        return true
    }
}