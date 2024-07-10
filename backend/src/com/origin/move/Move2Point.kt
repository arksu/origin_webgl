package com.origin.move

class Move2Point(me: MovingObject, _toX: Int, _toY: Int) : MoveController(me) {

    override val toX: Int = _toX
    override val toY: Int = _toY

    override suspend fun implementation(c: CollisionResult, left: Double, speed: Double, moveType: MoveType): Boolean {
        return when (c.result) {
            CollisionResult.CollisionType.COLLISION_NONE -> {
                true
            }
            CollisionResult.CollisionType.COLLISION_FAIL -> {
                // ошибка при обработке коллизии. надо остановить объект и удалить контроллер
                true
            }
            else -> {
                // коллизия с чем то. надо остановить работу и обработать результат
                true
            }
        }
    }
}
