package com.origin.model

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.util.Rect

/**
 * статичный игровой объект (дерево, ящик и тд)
 */
abstract class StaticObject(val objectRecord: ObjectRecord) : GameObject(
    objectRecord.id, ObjectPosition(
        objectRecord.x,
        objectRecord.y,
        objectRecord.level,
        objectRecord.region,
        objectRecord.heading
    )
) {
    val type get() = objectRecord.type

    override fun getBoundRect(): Rect {
        // TODO getBoundRect
        return Rect(4)
    }

}