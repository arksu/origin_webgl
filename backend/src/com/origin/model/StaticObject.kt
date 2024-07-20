package com.origin.model

import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.records.ObjectRecord
import com.origin.jooq.tables.references.OBJECT
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

    fun saveData() {
        DatabaseConfig.dsl
            .update(OBJECT)
            .set(OBJECT.DATA, record.data)
            .where(OBJECT.ID.eq(record.id))
            .execute()
    }

    override fun save() {
        DatabaseConfig.dsl
            .insertInto(OBJECT)
            .set(record)
            .returning()
            .fetchSingle()
    }

    override fun getHP(): Int {
        return record.hp
    }

    override fun setHP(hp: Int) {
        record.hp = hp
    }
}