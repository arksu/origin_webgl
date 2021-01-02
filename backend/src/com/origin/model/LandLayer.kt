package com.origin.model

import com.origin.entity.GridEntity
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * слой (уровень) земли
 */
@ObsoleteCoroutinesApi
class LandLayer(
    val region: Region,

    /**
     * уровень земли
     */
    val level: Int,

    /**
     * размеры в гридах
     */
    private val width: Int,
    private val height: Int,
) {
    /**
     * гриды
     */
    private val grids = ConcurrentLinkedQueue<Grid>()

    /**
     * найти грид среди загруженных
     */
    @Synchronized
    fun getGrid(gx: Int, gy: Int): Grid {
        if (gx < 0 || gy < 0) throw RuntimeException("wrong grid coords")

        // TODO:
        //  ищем ТУПО, но в будущем надо бы переделать на hashmap или еще как с компаратором
        //  по координатам
        //  а также предусмотреть выгрузку гридов из памяти и удаление из списка grids
        //  также неплохо было бы убрать Synchronized на методе, и блокировать только при загрузке грида
        //  ну а самый пик различать операцию получения грида из памяти и загрузку из базы
        //  в случае если грузим из базы сделать на suspend функциях
        //  так чтобы можно было параллельно запросить загрузку сразу нескольких гридов.
        //  и ждать когда они параллельно загрузятся, а не грузить по одному несколько штук

        grids.forEach { g ->
            if (g.x == gx && g.y == gy) return g
        }
        // если не нашли - тогда грузим из базы
        val grid = GridEntity.load(gx, gy, this)
        grids.add(grid)
        return grid
    }

    fun validateCoord(gx: Int, gy: Int): Boolean {
        return !(gx < 0 || gy < 0 || gx > width || gy > height)
    }
}