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
    val grids: List<Grid>? = null

    fun spawnPlayer(player: Player?): Boolean {
        // находим гриды которые нужны для спавна игрока
        return true
    }
}