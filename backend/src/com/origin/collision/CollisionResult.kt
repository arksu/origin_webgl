package com.origin.collision

class CollisionResult(val result: CollisionType) {

    companion object {
        val FAIL = CollisionResult(CollisionType.COLLISION_FAIL)
        val NONE = CollisionResult(CollisionType.COLLISION_NONE)
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
}
