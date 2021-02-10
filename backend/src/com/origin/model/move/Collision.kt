package com.origin.model.move

import com.origin.collision.CollisionResult
import com.origin.model.GameObject
import com.origin.model.Grid
import com.origin.net.logger
import com.origin.utils.TILE_SIZE
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.math.abs
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
        val movingArea = obj.getBoundRect().clone().move(obj.pos.point).extend(dx, dy)


        // получаем список объектов для обсчета коллизий из списка гридов
        val filtered = list.flatMap { it ->
            // фильтруем список объектов. вернем только те которые ТОЧНО МОГУТ дать коллизию
            // те которые далеко или не попадают в прямоугольник движения - отсечем их
            it.objects.filter {
                // сами себе никогда не даем коллизию
                if (obj == it) return@filter false
                // рект объекта
                val r = it.getBoundRect().clone().move(it.pos.point)
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


        var oldD = 0.0
        var dd: Double = 0.0
        var counter = 0
        while (true) {
            counter++
            // расстояние до конечной точки пути
            val d: Double = distance(curX, curY, toX.toDouble(), toY.toDouble())
            dd = (abs(oldD - oldD))
            oldD = d
            logger.debug("d ${String.format("%.2f", d)}")
            // осталось слишком мало. считаем что пришли. коллизий не было раз здесь
            when {
                d < 0.01 -> {
                    logger.debug("counter $counter")
                    if (isMove) {
                        obj.pos.setXY(toX, toY)
                    }
                    return CollisionResult.NONE
                }
                d < COLLISION_ITERATION_LENGTH -> {
                    // осталось идти меньше одной итерации. очередная точка это конечная
                    newX = toX.toDouble()
                    newY = toY.toDouble()
                    // после обсчета этой коллизии надо завершить цикл
                    needExit = true
                }
                else -> {
                    val k: Double = COLLISION_ITERATION_LENGTH / d
                    newX = curX + (toX - curX) * k
                    newY = curY + (toY - curY) * k
                }
            }
            logger.warn("new ${String.format("%.1f", newX)} ${String.format("%.1f", newY)}")

            fun testObjCollision(
                isMove: Boolean,
            ): CollisionResult? {
                val newXInt: Int = newX.roundToInt()
                val newYInt: Int = newY.roundToInt()

                // хитбокс объекта который движется
                val movingRect = obj.getBoundRect().clone().move(newXInt, newYInt)
                logger.debug("movingRect $movingRect")

                // проверяем коллизию с объектами
                filtered.forEach {
                    val ro = it.getBoundRect().clone().move(it.pos.point)
                    Grid.logger.debug("test $ro $movingRect")
                    if (movingRect.isIntersect(ro)) {
                        Grid.logger.debug("COLL!")
                        if (isMove) {
                            val oldNX = newX
                            val oldNY = newY

                            val ndx = newX - curX
                            val ndy = newY - curY
                            logger.warn("nd ${String.format("%.3f", ndx)} ${String.format("%.3f", ndy)}")

                            val threshold = 0
                            if (abs(ndy) > threshold) {
                                newX = curX
                                val r1 = testObjCollision(false)
                                logger.debug("r1 $r1")
                                if (r1 == null) {
                                    return null
                                }
                                newX = oldNX
                            }

                            if (abs(ndx) > threshold) {
                                newY = curY
                                val r2 = testObjCollision(false)
                                logger.debug("r2 $r2")
                                if (r2 == null) {
                                    return null
                                }
                                newY = oldNY
                            }

                            return CollisionResult(CollisionResult.CollisionType.COLLISION_OBJECT, null, it)
                        } else {
                            return CollisionResult(CollisionResult.CollisionType.COLLISION_OBJECT, null, it)
                        }
                    }
                }
                return null
            }

            val result = testObjCollision(isMove)
            if (result != null) {
                logger.debug("return by testObjCollision $result")
                obj.pos.setXY(curX.roundToInt(), curY.roundToInt())
                return result
            }

            if (needExit) {
                if (isMove) {
                    obj.pos.setXY(toX, toY)
                }
                logger.debug("counter $counter")
                return CollisionResult.NONE
            }

            logger.warn("dd ${String.format("%.1f", dd)} counter=$counter")



            if (isMove) {
//                if (dd < 0.001 && counter > 3) {
//                    obj.pos.setXY(curX.roundToInt(), curY.roundToInt())
//                    return CollisionResult.NONE
//                }
                if (counter > 25) {
                    if (isMove) {
                        obj.pos.setXY(curX.roundToInt(), curY.roundToInt())
                    }
                    return CollisionResult.NONE
                }
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