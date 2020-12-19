package com.origin.entity

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * игровой "чанк" (регион), базовый кусок карты
 * при больших объемах мира надо бить таблицу на партиции
 * по instance, суб партиции по x, y и тд
 */
object Grids : Table("grids") {
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
     * сырые данные тайлов в виде массива байт, по 2 байта на 1 тайл
     */
    val tilesBlob = blob("tiles")

    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(region, x, y, level) }

    override fun createStatement(): List<String> {
        return listOf(super.createStatement()[0] + " ENGINE=MyISAM")
    }
}

/**
 * not DAO, потому что у нас хитрый индекс без явного id поля
 */
class Grid(r: ResultRow) {
    var region = r[Grids.region]
    var x = r[Grids.x]
    var y = r[Grids.y]
    var level = r[Grids.level]
    var tilesBlob: ExposedBlob = r[Grids.tilesBlob]

    companion object {
        /**
         * загрузка грида из базы
         */
        fun load(gx: Int, gy: Int, level: Int, region: Int): Grid {
            val g = transaction {
                Grids.select { (Grids.x eq gx) and (Grids.y eq gy) and (Grids.level eq level) and (Grids.region eq region) }
                    .firstOrNull() ?: throw RuntimeException("")
            }
            return Grid(g)
        }
    }
}