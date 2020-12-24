package com.origin.entity

import com.origin.model.GameObject
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentLinkedQueue

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
class Grid(r: ResultRow) {
    //    var id = r[Grids.id]
    var region = r[Grids.region]
    var x = r[Grids.x]
    var y = r[Grids.y]
    var level = r[Grids.level]
    var lastTick = r[Grids.lastTick]
    var tilesBlob: ByteArray = r[Grids.tilesBlob].bytes

    /**
     * список активных объектов которые поддерживают этот грид активным
     */
    val activeObjects = ConcurrentLinkedQueue<GameObject>()

    /**
     * список объектов в гриде
     */
    val objects = ConcurrentLinkedQueue<GameObject>()

    companion object {
        /**
         * загрузка грида из базы
         */
        fun load(gx: Int, gy: Int, level: Int, region: Int): Grid {
            val row = transaction {
                Grids.select { (Grids.x eq gx) and (Grids.y eq gy) and (Grids.level eq level) and (Grids.region eq region) }
                    .firstOrNull() ?: throw RuntimeException("")
            }
            return Grid(row)
        }
    }
}