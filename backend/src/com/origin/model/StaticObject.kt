package com.origin.model

import com.origin.entity.EntityObject
import com.origin.utils.Rect
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@ObsoleteCoroutinesApi
abstract class StaticObject(val entity: EntityObject) :
    GameObject(entity.id.value, entity.x, entity.y, entity.level, entity.region, entity.heading) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(StaticObject::class.java)
    }

    init {
        spawned = true
    }

    val type = entity.type

    /**
     * взять у объекта хп
     */
    fun takeHP(value: Int): Boolean {
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

    fun getMaxHP(): Int {
        return 100
    }

    override fun getBoundRect(): Rect {
        // TODO getBoundRect
        return Rect(4)
    }
}