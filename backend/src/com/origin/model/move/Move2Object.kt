package com.origin.model.move

import com.origin.collision.CollisionResult
import com.origin.model.GameObject
import com.origin.model.MovingObject
import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * движение к объекту и взаимодействие с ним если дошли
 */
@ObsoleteCoroutinesApi
class Move2Object(me: MovingObject, target: GameObject, private val after: suspend () -> Unit = { }) :
    MoveController(me) {

    private val targetId: ObjectID = target.id

    override val toX: Int = target.pos.x

    override val toY: Int = target.pos.y

    override suspend fun implementation(c: CollisionResult, left: Double, speed: Double, moveType: MoveType): Boolean {
        when (c.result) {
            CollisionResult.CollisionType.COLLISION_FAIL -> {
                // ошибка при обработке коллизии. надо остановить объект и удалить контроллер
                me.stopMove()
                return true
            }
            CollisionResult.CollisionType.COLLISION_OBJECT -> {
                me.stopMove()
                if (c.obj != null && c.obj.id == targetId) {
                    // TODO линкуемся с объектом
                    after()
                }
                return true
            }
            else -> {
                // коллизия с чем то. надо остановить работу и обработать результат
                me.stopMove()
                return true
            }
        }
    }
}