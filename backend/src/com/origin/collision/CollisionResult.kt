package com.origin.collision

import com.origin.model.GameObject
import com.origin.utils.Vec2i
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
class CollisionResult(val result: CollisionType, val point: Vec2i?, val obj: GameObject?) {

    companion object {
        val FAIL = CollisionResult(CollisionType.COLLISION_FAIL, null, null)
        val NONE = CollisionResult(CollisionType.COLLISION_NONE, null, null)
    }


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

    override fun toString(): String {
        return "$result $point $obj"
    }
}
