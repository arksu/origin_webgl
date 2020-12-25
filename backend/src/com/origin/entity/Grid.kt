package com.origin.entity

import com.origin.model.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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
class Grid(r: ResultRow, val layer: LandLayer) {
    //    val id = r[Grids.id]
    val region = r[Grids.region]
    val x = r[Grids.x]
    val y = r[Grids.y]
    val level = r[Grids.level]
    var lastTick = r[Grids.lastTick]
    var tilesBlob: ByteArray = r[Grids.tilesBlob].bytes

    /**
     * список активных объектов которые поддерживают этот грид активным
     */
    private val activeObjects = ConcurrentLinkedQueue<Human>()

    /**
     * список объектов в гриде
     */
    private val objects = ConcurrentLinkedQueue<GameObject>()

    /**
     * блокировка для операций с гридом
     */
    val lock = ReentrantLock()

    /**
     * активен ли грид?
     */
    val isActive: Boolean get() = !activeObjects.isEmpty()

    /**
     * спавн объекта в грид
     */
    fun spawn(obj: GameObject): CollisionResult {
        if (obj.pos.region != region || obj.pos.level != level ||
            obj.pos.gridX != x || obj.pos.gridY != y
        ) {
            throw RuntimeException("wrong spawn condition")
        }

        lock.withLock {
            obj.lock.withLock {
                // в любом случае обновим грид до начала проверок коллизий
                update()

                // проверим коллизию с объектами и тайлами грида
                val collision = checkCollsion(obj, obj.pos.x, obj.pos.y, obj.pos.x, obj.pos.y, MoveType.SPAWN)

                if (collision.result == CollisionResult.CollisionType.COLLISION_NONE) {
                    if (obj is MovingObject) {
                        obj.activateGrids()
                    }
                    addObject(obj)
                }
                return collision
            }
        }
    }

    /**
     * обновление состояния грида и его объектов
     */
    fun update() {
        lock.withLock {

            // TODO

        }
    }

    /**
     * проверить коллизию
     */
    private fun checkCollsion(
        obj: GameObject,
        x: Int,
        y: Int,
        toX: Int,
        toY: Int,
        moveType: MoveType,
    ): CollisionResult {
        lock.withLock {

            // TODO

            return CollisionResult.NONE
        }
    }

    /**
     * добавить объект в грид
     * перед вызовом грид обязательно должен быть залочен!!!
     */
    private fun addObject(obj: GameObject) {
        if (!lock.isLocked) {
            throw RuntimeException("addObject: grid is not locked")
        }

        if (!objects.contains(obj)) {
            objects.add(obj)

            if (isActive) activeObjects.forEach {
                it.onObjectAdded(obj)
            }
        }
    }

    /**
     * удалить объект из грида
     */
    fun removeObject(obj: GameObject) {
        if (!lock.isLocked) {
            throw RuntimeException("addObject: grid is not locked")
        }

        if (objects.contains(obj)) {
            obj.onRemove()
            objects.remove(obj)

            if (isActive) activeObjects.forEach {
                it.onObjectRemoved(obj)
            }
        }
    }

    /**
     * активировать грид
     * только пока есть хоть 1 объект связанный с гридом - он будет считатся активным
     * если ни одного объекта нет грид становится не активным и не обновляет свое состояние
     * @param human объект который связывается с гридом
     * @return только если удалось активировать
     */
    fun activate(human: Human) {
        if (!activeObjects.contains(human)) {
            if (!isActive) {
                update()
            }

            activeObjects.add(human)
        }
    }

    /**
     * деактивировать грид
     * если в гриде не осталось ни одного активного объекта то он прекращает обновляться
     */
    fun deactivate(human: Human) {

    }

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
    }
}