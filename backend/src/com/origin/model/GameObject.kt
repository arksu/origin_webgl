package com.origin.model

import com.origin.entity.EntityPosition
import com.origin.entity.Grid
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * базовый игровой объект в игровой механике
 * все игровые сущности наследуются от него
 */
open class GameObject(entityPosition: EntityPosition) {

    /**
     * координаты кэшируем в объекте (потом периодически обновляем в сущности)
     */
    val pos: Position = Position(entityPosition.x,
        entityPosition.y,
        entityPosition.level,
        entityPosition.region,
        entityPosition.heading,
        this)

    /**
     * блокировка для операций с объектом
     */
    val lock = ReentrantLock()

    /**
     * текущий активный грид в котором находится объект
     */
    protected val grid: Grid? get() = pos.grid

    /**
     * объект который несем над собой, или в котором едем. по сути это контейнер для вложенных объектов
     * они больше не находятся в гриде, а обслуживаются только объектом который их "несет/везет"
     * причем такое состояние только в рантайме. в базе все хранится по координатам. и при рестарте сервера
     * все будет спавнится в одни и теже координаты
     */
    private val lift = ConcurrentHashMap<Int, GameObject>()

    /**
     * удалить объект из мира
     */
    fun remove() {
        lock.withLock {
            pos.grid?.removeObject(this)

            // если есть что-то вложенное внутри
            if (!lift.isEmpty()) {
                lift.values.forEach {
                    // TODO
//                    it.pos.set xy coord
                    // spawn it
                    // store pos into db
                }
            }
        }
    }

    /**
     * когда этот объект удален из грида
     */
    fun onRemove() {
        // TODO known list
    }
}