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

    @Synchronized
    fun spawnPlayer(player: Player): Boolean {
        // грид в котором находится игрок
        val grid = getGrid(player.pos.gridX, player.pos.gridY)

        // находим гриды которые нужны для спавна игрока
        

        return true
    }

    /**
     * найти грид среди загруженных
     */
    @Synchronized
    private fun getGrid(gx: Int, gy: Int): Grid {
        // ищем ТУПО, но в будущем надо бы переделать на hashmap
        // а также предусмотреть выгрузку гридов из памяти и удаление из списка grids
        grids.forEach { g ->
            if (g.x == gx && g.y == gy) return g
        }
        // если не нашли - тогда грузим из базы
        return Grid.load(gx, gy, level, region.id)
    }
}