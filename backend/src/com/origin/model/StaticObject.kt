package com.origin.model

import com.origin.jooq.tables.records.ObjectRecord
import com.origin.util.Rect

/**
 * статичный игровой объект (дерево, ящик и тд)
 */
abstract class StaticObject(val record: ObjectRecord) : GameObject(
    record.id, ObjectPosition(
        record.x,
        record.y,
        record.level,
        record.region,
        record.heading
    )
) {
    val type get() = record.type

    override fun getBoundRect(): Rect {
        // TODO getBoundRect
        return Rect(4)
    }

}