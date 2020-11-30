package com.origin.entity

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.ExposedBlob

/**
 * игровой "чанк" (регион), базовый кусок карты
 * при больших объемах мира надо бить таблицу на партиции
 * по instance, суб партиции по x, y и тд
 */
object Grids : Table("grids") {
    /**
     * на каком континенте находится грид, либо ид дома (инстанса, локации)
     */
    val region: Column<Int> = integer("region")

    /**
     * координаты грида в мире (какой по счету грид, НЕ в игровых единицах)
     * разбиение таблицы (partitions) делаем на основе RANGE(x) и субпартициях на основе RANGE(y)
     */
    val x: Column<Int> = integer("x")
    val y: Column<Int> = integer("y")
    val level: Column<Int> = integer("level")

    /**
     * сырые данные тайлов в виде массива байт, по 2 байта на 1 тайл
     */
    val tilesBlob: Column<ExposedBlob> = blob("tiles")

    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(region, x, y, level) }

    override fun createStatement(): List<String> {
        return listOf(super.createStatement()[0] + " ENGINE=MyISAM")
    }
}

class Grid {
    var region = 0
    var x = 0
    var y = 0
    var level = 0
    var tilesBlob: ExposedBlob? = null
}