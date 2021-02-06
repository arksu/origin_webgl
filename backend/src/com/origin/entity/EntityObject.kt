package com.origin.entity

import com.origin.TimeController
import com.origin.idfactory.IdFactory
import com.origin.model.move.Position
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

/**
 * игровые объекты на карте
 */
object EntityObjects : EntityPositions("objects") {

    /**
     * координаты грида в котором находится объект
     */
    val gridx = integer("gridx")
    val gridy = integer("gridy")

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
    val hp = integer("hp").default(100)

    /**
     * время создания, игровой тик сервера
     * @see com.origin.TimeController.tickCount
     */
    val createTick = long("createTick")

    /**
     * время последнего апдейта объекта
     * если логика объекта требует периодического обновления
     */
    val lastTick = long("lastTick").default(0)

    /**
     * внутренние данные объекта в json формате
     */
    val data = text("data").nullable()

    init {
        index(false, region, x, y, level)
    }
}

@ObsoleteCoroutinesApi
class EntityObject(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<EntityObject>(EntityObjects) {
        fun makeNew(pos: Position, t: Int, q: Short = 10): EntityObject {
            return EntityObject.new(IdFactory.getNext()) {
                x = pos.x
                y = pos.y
                gridx = pos.gridX
                gridy = pos.gridY
                level = pos.level
                region = pos.region
                heading = pos.heading
                type = t
                quality = q
                createTick = TimeController.tickCount
            }
        }
    }

    var region by EntityObjects.region
    var x by EntityObjects.x
    var y by EntityObjects.y
    var level by EntityObjects.level
    var heading by EntityObjects.heading

    var gridx by EntityObjects.gridx
    var gridy by EntityObjects.gridy

    var type by EntityObjects.type
    var quality by EntityObjects.quality
    var hp by EntityObjects.hp
    var createTick by EntityObjects.createTick
    var lastTick by EntityObjects.lastTick
    var data by EntityObjects.data
}