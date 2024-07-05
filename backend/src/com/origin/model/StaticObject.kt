package com.origin.model

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.util.Rect

/**
 * статичный игровой объект (дерево, ящик и тд)
 */
class StaticObject(val objectRecord: ObjectRecord) : GameObject(
    objectRecord.id, ObjectPosition(
        objectRecord.x,
        objectRecord.y,
        objectRecord.level,
        objectRecord.region,
        objectRecord.heading
    )
) {
    override fun getBoundRect(): Rect {
        // TODO getBoundRect
        return Rect(4)
    }

}