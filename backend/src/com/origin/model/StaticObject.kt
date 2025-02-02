package com.origin.model

import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.records.ObjectRecord
import com.origin.jooq.tables.references.OBJECT
import com.origin.util.Rect

/**
 * статичный игровой объект (дерево, ящик и тд)
 */
abstract class StaticObject(val entity: ObjectRecord) : GameObject(
    entity.id, ObjectPosition(
        entity.x,
        entity.y,
        entity.level,
        entity.region,
        entity.heading
    )
) {
    val type get() = entity.type
    val quality get() = entity.quality

    override fun getBoundRect(): Rect {
        // TODO getBoundRect
        return Rect(4)
    }

    open fun saveData() {
        DatabaseConfig.dsl
            .update(OBJECT)
            .set(OBJECT.DATA, entity.data)
            .where(OBJECT.ID.eq(entity.id))
            .execute()
    }

    override fun save() {
        DatabaseConfig.dsl
            .insertInto(OBJECT)
            .set(entity)
            .returning()
            .fetchSingle()
    }

    override fun getHP(): Int {
        return entity.hp
    }

    override fun setHP(hp: Int) {
        entity.hp = hp
    }
}