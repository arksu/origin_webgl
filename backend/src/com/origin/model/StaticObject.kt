package com.origin.model

import com.origin.jooq.tables.records.ObjectRecord

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
}