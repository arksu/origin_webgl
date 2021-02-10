package com.origin.model.move

import com.origin.collision.CollisionResult
import com.origin.model.GameObject
import com.origin.model.Grid
import com.origin.net.logger
import com.origin.utils.Rect
import com.origin.utils.TILE_SIZE
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
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

    fun process(
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

        Grid.logger.warn("filtered [${filtered.size}]:")
        filtered.forEach {
            Grid.logger.debug("$it")
        }

        var curX: Double = obj.pos.x.toDouble()
        var curY: Double = obj.pos.y.toDouble()
        var newX: Double
        var newY: Double
        var needExit = false

        logger.debug("obj $obj d $dx, $dy movingArea $movingArea")

        var counter = 0
        while (true) {
            counter++
            // расстояние до конечной точки пути
            val d: Double = distance(curX, curY, toX.toDouble(), toY.toDouble())
            logger.debug("d $d")
            // осталось слишком мало. считаем что пришли. коллизий не было раз здесь
            when {
                d < 0.01 -> {
                    logger.debug("counter $counter")
                    return CollisionResult.NONE
                }
                d < COLLISION_ITERATION_LENGTH -> {
                    // осталось идти меньше одной итерации. очередная точка это конечная
                    newX = toX.toDouble()
                    newY = toY.toDouble()
                    needExit = true
                }
                else -> {
                    val k: Double = COLLISION_ITERATION_LENGTH / d
                    newX = curX + (toX - curX) * k
                    newY = curY + (toY - curY) * k
                }
            }
            val curXint: Int = newX.roundToInt()
            val curYint: Int = newY.roundToInt()

            // хитбокс объекта который движется
            val movingRect = Rect(obj.getBoundRect()).move(curXint, curYint)
            logger.debug("movingRect $movingRect")

            // проверяем коллизию с объектами
            filtered.forEach {
                val ro = it.getBoundRect().clone().move(it.pos.point)
                Grid.logger.debug("test $ro $movingRect")
                if (movingRect.isIntersect(ro)) {
                    Grid.logger.debug("COLL!")
                    if (isMove) {
                        runBlocking {
                            obj.pos.setXY(curXint, curYint)
                        }
                    }
                    logger.debug("counter $counter")
                    return CollisionResult(CollisionResult.CollisionType.COLLISION_OBJECT, null, it)
                }
            }

            if (needExit) {
                if (isMove) {
                    runBlocking {
                        obj.pos.setXY(toX, toY)
                    }
                }
                logger.debug("counter $counter")
                return CollisionResult.NONE
            }

            curX = newX
            curY = newY
        }
    }

    private fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }
}