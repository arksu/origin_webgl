package com.origin.model.move

import com.origin.model.GameObject
import com.origin.model.MovingObject
import com.origin.model.OPEN_DISTANCE
import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * движение к объекту и взаимодействие с ним если дошли
 */
@ObsoleteCoroutinesApi
class Move2Object(me: MovingObject, private val target: GameObject, private val after: suspend () -> Unit = { }) :
    MoveController(me) {

    private val targetId: ObjectID = target.id

    override val toX: Int = target.pos.x

    override val toY: Int = target.pos.y

    override suspend fun implementation(c: CollisionResult, left: Double, speed: Double, moveType: MoveType): Boolean {
        when (c.result) {
            CollisionResult.CollisionType.COLLISION_NONE -> {
                val myRect = me.getBoundRect().clone().move(me.pos.point)
                val objRect = target.getBoundRect().clone().move(target.pos.point)
                val (mx, my) = myRect.min(objRect)
                if (mx <= OPEN_DISTANCE && my <= OPEN_DISTANCE) {
                    after()
                }
                return true
            }
            CollisionResult.CollisionType.COLLISION_FAIL -> {
                // ошибка при обработке коллизии. надо остановить объект и удалить контроллер
                return true
            }
            CollisionResult.CollisionType.COLLISION_OBJECT -> {
                if (c.obj != null && c.obj.id == targetId) {
                    after()
                }
                return true
            }
            else -> {
                // коллизия с чем то. надо остановить работу и обработать результат
                return true
            }
        }
    }
}
