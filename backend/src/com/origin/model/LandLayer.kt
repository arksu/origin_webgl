package com.origin.model

import com.origin.GRID_FULL_SIZE
import java.util.concurrent.ConcurrentHashMap

/**
 * слой (уровень) земли
 */
class LandLayer(
    val region: Region,

    /**
     * уровень земли
     */
    val level: Int,

    /**
     * размеры в гридах
     */
    val width: Int,
    val height: Int,
) {
    /**
     * гриды
     */
    private val grids = ConcurrentHashMap<String, Grid>()

    /**
     * найти грид среди загруженных
     */
    @Synchronized
    fun getGrid(gx: Int, gy: Int): Grid {
        if (gx < 0 || gy < 0) throw RuntimeException("wrong grid coords")

        // TODO: getGrid
        //  предусмотреть выгрузку гридов из памяти и удаление из списка grids
        //  также неплохо было бы убрать Synchronized на методе, и блокировать только при загрузке грида
        //  ну а самый пик различать операцию получения грида из памяти и загрузку из базы
        //  в случае если грузим из базы сделать на suspend функциях
        //  так чтобы можно было параллельно запросить загрузку сразу нескольких гридов.
        //  и ждать когда они параллельно загрузятся, а не грузить по одному

        return grids.getOrPut("$gx*$gy") { Grid.load(gx, gy, this) }
    }

    /**
     * валидация координат гридов (не выходит ли за пределы слоя)
     */
    fun validateCoord(gx: Int, gy: Int): Boolean {
        return !(gx < 0 || gy < 0 || gx >= width || gy >= height)
    }

    /**
     * валидация абсолютных мировых координат
     */
    fun validateAbsoluteCoord(x: Int, y: Int): Boolean {
        return !(x < 0 || y < 0 || x >= width * GRID_FULL_SIZE || y >= height * GRID_FULL_SIZE)
    }
}
