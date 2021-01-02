package com.origin.entity

import com.origin.model.Grid
import com.origin.model.LandLayer
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

/**
 * игровой "чанк" (регион), базовый кусок карты
 * при больших объемах мира надо бить таблицу на партиции
 * по instance, суб партиции по x, y и тд
 */
object Grids : Table("grids") {
//    val id: Column<Int> = integer("id").autoIncrement()

    /**
     * на каком континенте находится грид, либо ид дома (инстанса, локации)
     */
    val region = integer("region")

    /**
     * координаты грида в мире (какой по счету грид, НЕ в игровых единицах)
     * разбиение таблицы (partitions) делаем на основе RANGE(x) и субпартициях на основе RANGE(y)
     */
    val x = integer("x")
    val y = integer("y")
    val level = integer("level")

    /**
     * время последнего обновления (в игровых тиках)
     */
    val lastTick = integer("lastTick")

    /**
     * сырые данные тайлов в виде массива байт, по 2 байта на 1 тайл
     */
    val tilesBlob = blob("tiles")

//    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(id) }

    init {
        uniqueIndex(region, x, y, level)
    }

    override fun createStatement(): List<String> {
        return listOf(super.createStatement()[0] + " ENGINE=MyISAM")
    }
}

/**
 * НЕ DAO, потому что у нас хитрый индекс без явного id поля
 */
@ObsoleteCoroutinesApi
open class GridEntity(r: ResultRow, val layer: LandLayer) {
    //    val id = r[Grids.id]
    val region = r[Grids.region]
    val x = r[Grids.x]
    val y = r[Grids.y]
    val level = r[Grids.level]
    var lastTick = r[Grids.lastTick]
    var tilesBlob: ByteArray = r[Grids.tilesBlob].bytes

    companion object {
        /**
         * загрузка грида из базы
         */
        fun load(gx: Int, gy: Int, layer: LandLayer): Grid {
            val row = transaction {
                Grids.select { (Grids.x eq gx) and (Grids.y eq gy) and (Grids.level eq layer.level) and (Grids.region eq layer.region.id) }
                    .firstOrNull() ?: throw RuntimeException("")
            }
            return Grid(row, layer)
        }

        val logger = LoggerFactory.getLogger(Grid::class.java)
    }
}