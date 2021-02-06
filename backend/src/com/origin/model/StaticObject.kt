package com.origin.model

import com.origin.entity.EntityObject
import com.origin.utils.Rect
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.transactions.transaction

@ObsoleteCoroutinesApi
open class StaticObject(val entity: EntityObject) :
    GameObject(entity.id.value, entity.x, entity.y, entity.level, entity.region, entity.heading) {

    val type = entity.type

    /**
     * взять у объекта хп
     */
    fun takeHp(value: Int): Boolean {
        if (value <= entity.hp) {
            val old = entity.hp
            transaction {
                entity.hp -= value
            }
            logger.debug("takeHp $old ->  ${entity.hp}")
            return entity.hp > 0
        }
        return false
    }

    override fun getBoundRect(): Rect {
        // TODO getBoundRect
        return Rect(4)
    }

    override fun getResourcePath(): String {
        return "unknown"
    }
}