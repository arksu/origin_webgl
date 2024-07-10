package com.origin.model.`object`

import com.origin.IdFactory
import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.records.ObjectRecord
import com.origin.jooq.tables.references.OBJECT
import com.origin.model.GameObject
import com.origin.model.`object`.tree.Birch
import com.origin.move.PositionModel

object ObjectsFactory {
    fun constructByRecord(record: ObjectRecord): GameObject {
        return when (record.type) {
            1 -> Birch(record)
            else -> UnknownObject(record)
        }
    }

    fun create(type: Int, pos: PositionModel): ObjectRecord {
        val record = ObjectRecord(
            id = IdFactory.getNext(),
            region = pos.region,
            x = pos.x,
            y = pos.y,
            level = pos.level,
            heading = pos.heading,
            gridX = pos.gridX,
            gridY = pos.gridY,
            type = type,
            quality = 10,
            hp = 100,
            createTick = 0,
            lastTick = 0,
            data = null
        )
        val saved = DatabaseConfig.dsl
            .insertInto(OBJECT)
            .set(record)
            .returning()
            .fetchSingle()
        return saved
    }
}