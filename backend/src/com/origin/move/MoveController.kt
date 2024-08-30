package com.origin.move

import com.origin.TimeController
import com.origin.config.ServerConfig
import com.origin.model.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.math.pow

/**
 * реализует передвижения объектов
 * расчитывает новую позицию. ставит ее объекту и уведомляет всех о смене позиции
 */
abstract class MoveController(val me: MovingObject) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(MoveController::class.java)
    }

    abstract val toX: Int
    abstract val toY: Int

    /**
     * последняя сохраненная в базу позиция
     */
    private var storedX: Double = me.pos.x.toDouble()
    private var storedY: Double = me.pos.y.toDouble()

    /**
     * время последнего апдейта движения (в системных мсек)
     */
    private var lastMoveTime = System.currentTimeMillis()

    /**
     * внутренняя реализация движения. надо определить куда должны передвинутся за тик
     * @return движение завершено? (истина ежели уперлись во чтото или прибыли в пункт назначения)
     */
    abstract suspend fun implementation(c: CollisionResult, left: Double, speed: Double, moveType: MoveType): Boolean

    /**
     * начать работу контроллера (при начале движения)
     */
    open suspend fun start() {
        lastMoveTime = System.currentTimeMillis()
        TimeController.addMovingObject(me)

        // в самом начале движения пошлем пакет о том что объект уже начал движение
        me.getGridSafety().broadcast(
            BroadcastEvent.StartMove(
                me, toX, toY, me.getMovementSpeed(), me.getMovementType()
            )
        )
    }

    /**
     * остановить работу контроллера (принудительная остановка извне)
     */
    fun stop() {
        TimeController.deleteMovingObject(me)
    }

    /**
     * возможно ли начать движение
     */
    fun canStartMoving(): Boolean {
        // берем новую точку через 1 тик
        // чтобы убедиться, что мы можем туда передвигаться
//        val dist = calcDistance(0.2 / TimeController.TICKS_PER_SECOND, me.getMovementSpeed())

//        logger.debug("nx=$nx ny=$ny")
//        if (nx == x && ny == y) {
//            return false
//        }

        // проверим коллизию с этой новой точкой
//        val c = checkCollision(toX, toY, dist, null, false)

        // можем двигаться только если коллизии нет
//        return c.result == CollisionResult.CollisionType.COLLISION_NONE
        return true
    }

    /**
     * обработать тик передвижения
     * @return движение завершено? (истина ежели уперлись во чтото или прибыли в пункт назначения)
     */
    suspend fun updateAndResult(): Boolean {
        logger.debug("updateAndResult")
        val currentTime = System.currentTimeMillis()
        if (currentTime > lastMoveTime) {
            // узнаем сколько времени прошло между апдейтами
            val deltaTime: Double = (currentTime - lastMoveTime) / 1000.0
            // если проходит слишком мало времени у нас дельта движения будет нулевая - и будет остановка по триггеру abs(ad) < 0.35
            // поэтому исключаем обсчет когда прошло еще мало времени для сколь нибудь значительного передвижения
            if (deltaTime < 0.030) return false
            lastMoveTime = currentTime

            // запомним тип движеня на начало обсчетов. возможно он изменится после
            val moveType = me.getMovementType()
            // также запомним скорость с которой шли
            val speed = me.getMovementSpeed()
            // очередная точка на пути
            val dist = calcDistance(deltaTime, speed)

            // сколько осталось идти до конечной точки
            val left = me.pos.dist(toX, toY)

            logger.warn("MOVE ($toX $toY) -> ($left) me ${me.pos}")
            // проверим коллизию при движении в новую точку
            val c = checkCollision(toX, toY, dist, null, true)

            // обработаем ситуацию когда нет коллизий при движении, она общая для всех типов движения
            val wasStopped = if (c.result == CollisionResult.CollisionType.COLLISION_NONE) {
                // сколько осталось до конечной точки после обсчета коллизии
                val actualLeft = me.pos.dist(toX, toY)
                val ad = abs(actualLeft - left)
                logger.debug("ad=$ad left=$left actualLeft=$actualLeft")
                when {
                    // расстояние до конечной точки при котором считаем что уже дошли куда надо
                    actualLeft <= 1.0 -> {
                        implementation(c, left, speed, moveType)
                    }
                    // в ходе обсчета коллизии мы сдвинулись. но сдвинулись на малое расстояние
                    abs(ad) < 0.35 -> {
                        implementation(c, left, speed, moveType)
                    }

                    else -> {
                        // продолжаем движение
                        if (me is Human) {
                            // обновляем видимые объекты при каждом передвижении
                            me.updateVisibleObjects(false)
                        }
                        // шлем через грид эвент передвижения
                        me.getGridSafety().broadcast(
                            BroadcastEvent.Moved(
                                me, toX, toY, speed, moveType
                            )
                        )
                        false
                    }
                }
                // в implementation обрабатываем ситуации с коллизиями
            } else implementation(c, left, speed, moveType)

            logger.debug("wasStopped $wasStopped")
            // если движение не завершено - обновляем позицию в базе
            if (wasStopped) {
                me.stopMove()
            } else {
                val dx: Double = me.pos.x - storedX
                val dy: Double = me.pos.y - storedY
                // logger.debug("move dx=$dx dy=$dy d=${sqrt(dx.pow(2) + dy.pow(2))}")

                // если передвинулись достаточно далеко
                if (dx.pow(2) + dy.pow(2) > ServerConfig.UPDATE_DB_DISTANCE.toDouble().pow(2)) {
                    me.storePositionInDb()
                    storedX = me.pos.x.toDouble()
                    storedY = me.pos.y.toDouble()
                }
            }
            return wasStopped
        }
        return false
    }

    /**
     * проверить коллизию и передвинуться через текущий грид
     * всю работу выполняет грид, т.к. объекты для коллизий хранятся только там
     * позицию изменит тоже он если isMove=true
     */
    private suspend fun checkCollision(
        toX: Int,
        toY: Int,
        dist: Double,
        virtual: GameObject?,
        isMove: Boolean,
    ): CollisionResult {
        // шлем сообщение гриду о необходимости проверить коллизию
        return me.getGridSafety().sendAndWaitAck(GridMessage.CheckCollision(me, toX, toY, dist, me.getMovementType(), virtual, isMove))
    }

    /**
     * расчитать новую точку движения на основе изменения времени
     */
    private fun calcDistance(deltaTime: Double, speed: Double): Double {
        // расстояние оставшееся до конечной точки
        val td = me.pos.dist(toX, toY)

        // сколько прошли: либо расстояние пройденное за тик, либо оставшееся до конечной точки. что меньше
        val distance = (deltaTime * speed).coerceAtMost(td)
        logger.warn("calcNewPoint $deltaTime $distance")

        return distance
    }
}
