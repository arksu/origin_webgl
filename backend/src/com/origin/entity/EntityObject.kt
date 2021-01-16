package com.origin.entity

import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

/**
 * игровые объекты на карте
 */
object EntityObjects : EntityPositions("objects") {

    /**
     * тип объекта
     */
    val type = integer("type")

    /**
     * качество
     */
    val quality = short("quality").default(10)

    /**
     * здоровье (hit points) объекта
     */
    val hp = short("hp").default(100)

    /**
     * время создания, игровой тик сервера
     * @see com.origin.TimeController.tickCount
     */
    val createTick = integer("createTick")

    /**
     * время последнего апдейта объекта
     * если логика объекта требует периодического обновления
     */
    val lastTick = integer("lastTick").default(0)

    /**
     * внутренние данные объекта в json формате
     */
    val data = text("data")

    init {
        index(false, region, x, y, level)
    }
}

class EntityObject(id: EntityID<Long>) : EntityPosition(id) {
    companion object : LongEntityClass<EntityObject>(EntityObjects)

    var type by EntityObjects.type
    var quality by EntityObjects.quality
    var hp by EntityObjects.hp
    var createTick by EntityObjects.createTick
    var lastTick by EntityObjects.lastTick
    var data by EntityObjects.data
}