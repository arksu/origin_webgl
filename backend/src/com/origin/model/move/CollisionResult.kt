package com.origin.model.move

import com.origin.model.GameObject
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class CollisionResult(val result: CollisionType, val px: Double, val py: Double, val obj: GameObject?) {

    companion object {
        val FAIL = CollisionResult(CollisionType.COLLISION_FAIL, 0.0, 0.0, null)
        val NONE = CollisionResult(CollisionType.COLLISION_NONE, 0.0, 0.0, null)
    }

    constructor(result: CollisionType, obj: GameObject?) : this(result, 0.0, 0.0, obj)

    enum class CollisionType {
        // обсчет коллизии не успешен
        COLLISION_FAIL,

        // нет коллизий
        COLLISION_NONE,

        // коллизия с тайлом
        COLLISION_TILE,

        // виртуальная коллизия
        COLLISION_VIRTUAL,

        // коллизия с объектом
        COLLISION_OBJECT,

        // с концом мира
        COLLISION_WORLD
    }

    fun isObject(): Boolean {
        return result == CollisionType.COLLISION_OBJECT
    }

    fun isNone(): Boolean {
        return result == CollisionType.COLLISION_NONE
    }

    override fun toString(): String {
        return "$result $px $py $obj"
    }
}
