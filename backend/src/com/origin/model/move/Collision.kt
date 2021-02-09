package com.origin.model.move

import com.origin.collision.CollisionResult
import com.origin.model.GameObject
import com.origin.model.Grid
import com.origin.utils.Rect
import com.origin.utils.TILE_SIZE
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.math.roundToInt
import kotlin.math.sqrt

@ObsoleteCoroutinesApi
object Collision {
    /**
     * сколько тайлов до конца мира будут давать коллизию
     */
    val WORLD_BUFFER_SIZE = TILE_SIZE * 5

    /**
     * расстояние в единицах игровых координат между итерациями
     */
    val COLLISION_ITERATION_LENGTH = 3.0

    suspend fun process(
        toX: Int,
        toY: Int,
        obj: GameObject,
        list: Array<Grid>,
        isMove: Boolean,
        tries: Int,
    ): CollisionResult {
        Grid.logger.debug("to $toX $toY")

        // определяем вектор движения для отсечения объектов которые находятся за пределами вектора
        val dx = toX - obj.pos.x
        val dy = toY - obj.pos.y
        // прямоугольник по границам объекта захватывающий начальную и конечную точку движения
        val movingArea = Rect(obj.getBoundRect()).move(obj.pos.point).extend(dx, dy)


        //            logger.debug("obj $obj d $dx, $dy move rect $mr")
        //            logger.warn("obj rect $or")

        // получаем список объектов для обсчета коллизий из списка гридов
        val filtered = list.flatMap { it ->
            // фильтруем список объектов. вернем только те которые ТОЧНО МОГУТ дать коллизию
            // те которые далеко или не попадают в прямоугольник движения - отсечем их
            it.objects.filter {
                // сами себе никогда не даем коллизию
                if (obj == it) return@filter false
                // рект объекта
                val r = Rect(it.getBoundRect()).move(it.pos.point)
                // границы объекта должны быть в границах вектора движения объекта
                // то есть пересекаться с областью движения объекта
                movingArea.isIntersect(r)
            }
        }

        Grid.logger.warn("filtered:")
        filtered.forEach {
            Grid.logger.debug("$it")
        }

        var curX: Double = obj.pos.x.toDouble()
        var curY: Double = obj.pos.y.toDouble()
        var newX: Double
        var newY: Double
        var needExit = false

        while (true) {
            // расстояние до конечной точки пути
            val d: Double = distance(curX, curY, toX.toDouble(), toY.toDouble())
            // осталось слишком мало. считаем что пришли. коллизий не было раз здесь
            if (d < 0.01) {
                return CollisionResult.NONE
            } else if (d < COLLISION_ITERATION_LENGTH) {
                // осталось идти меньше одной итерации. очередная точка это конечная
                newX = toX.toDouble()
                newY = toY.toDouble()
                needExit = true
            } else {
                val k: Double = COLLISION_ITERATION_LENGTH / d
                newX = curX + (toX - curX) * k
                newY = curY + (toX - curY) * k
            }
            val curXint: Int = newX.roundToInt()
            val curYint: Int = newY.roundToInt()

            // хитбокс объекта который движется
            val movingRect = Rect(obj.getBoundRect()).move(curXint, curYint)
            filtered.forEach {
                val ro = it.getBoundRect().clone().move(it.pos.point)
                if (movingRect.isIntersect(ro)) {
                    if (isMove) {
                        obj.pos.setXY(curXint, curYint)
                    }
                    return CollisionResult(CollisionResult.CollisionType.COLLISION_OBJECT, null, it)
                }
            }

            if (needExit) {
                return CollisionResult.NONE
            }

            curX = newX
            curY = newY
        }

//        if (isMove) {
//            obj.pos.setXY(toX, toY)
//        }
//        return CollisionResult.NONE
    }

    private fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }
}