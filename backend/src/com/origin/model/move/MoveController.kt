package com.origin.model.move

import com.origin.ServerConfig
import com.origin.TimeController
import com.origin.collision.CollisionResult
import com.origin.model.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * реализует передвижения объектов
 * расчитывает новую позицию. ставит ее объекту и уведомляет всех о смене позиции
 */
@ObsoleteCoroutinesApi
abstract class MoveController(val me: MovingObject) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(MoveController::class.java)
    }

    val x
        get() = me.pos.x

    val y
        get() = me.pos.y

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
        me.pos.grid.broadcast(BroadcastEvent.StartMove(
            me, toX, toY, me.getMovementSpeed(), me.getMovementType()
        ))
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
    suspend fun canStartMoving(): Boolean {
        // берем новую точку через 1 тик
        // чтобы убедиться что мы можем туда передвигаться
        val (nx, ny) = calcNewPoint(0.2 / TimeController.TICKS_PER_SECOND, me.getMovementSpeed())

        logger.debug("nx=$nx ny=$ny")
//        if (nx == x && ny == y) {
//            return false
//        }

        // проверим коллизию с этой новой точкой
        val c = checkCollision(nx, ny, null, false)

        // можем двигаться только если коллизии нет
        return c.result == CollisionResult.CollisionType.COLLISION_NONE
    }

    /**
     * обработать тик передвижения
     * @return движение завершено? (истина ежели уперлись во чтото или прибыли в пункт назначения)
     */
    suspend fun updateAndResult(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime > lastMoveTime) {
            // узнаем сколько времени прошло между апдейтами
            val deltaTime: Double = (currentTime - lastMoveTime) / 1000.0
            lastMoveTime = currentTime

            // запомним тип движеня на начало обсчетов. возможно он изменится после
            val moveType = me.getMovementType()
            // также запомним скорость с которой шли
            val speed = me.getMovementSpeed()
            // очередная точка на пути
            val (nx, ny) = calcNewPoint(deltaTime, speed)

            // проверим коллизию при движении в новую точку
            val c = checkCollision(nx, ny, null, true)

            // сколько осталось идти до конечной точки
            val left = sqrt((toX - x).toDouble().pow(2) + (toY - y).toDouble().pow(2))

            // обработаем ситуацию когда нет коллизий при движении, она общая для всех типов движения
            val wasStopped = if (c.result == CollisionResult.CollisionType.COLLISION_NONE) {
                // расстояние до конечной точки при котором считаем что уже дошли куда надо
                return if (left <= 1.0) {
                    me.stopMove()
                    true
                } else {
                    // продолжаем движение
                    if (me is Human) {
                        // обновляем видимые объекты при каждом передвижении
                        me.updateVisibleObjects(false)
                    }
                    // шлем через грид эвент передвижения
                    me.pos.grid.broadcast(BroadcastEvent.Moved(
                        me, toX, toY, speed, moveType
                    ))
                    false
                }
                // в implementation обрабатываем ситуации с коллизиями
            } else implementation(c, left, speed, moveType)

            // если движение не завершено - обновляем позицию в базе
            if (wasStopped) {
                // движение завершено. внутри implementation сохранили позицию в базе, запомним и тут
                storedX = me.pos.x.toDouble()
                storedY = me.pos.y.toDouble()
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
    private suspend fun checkCollision(toX: Int, toY: Int, virtual: GameObject?, isMove: Boolean): CollisionResult {
        // шлем сообщение гриду о необходимости проверить коллизию
        val resp = CompletableDeferred<CollisionResult>()
        me.pos.grid.send(GridMsg.CheckCollision(me, toX, toY, me.getMovementType(), virtual, isMove, resp))
        return resp.await()
    }

    /**
     * расчитать новую точку движения на основе изменения времени
     */
    private fun calcNewPoint(deltaTime: Double, speed: Double): Pair<Int, Int> {
        val tdx = (toX - x).toDouble()
        val tdy = (toY - y).toDouble()

        // расстояние оставшееся до конечной точки
        val td = sqrt(tdx.pow(2) + tdy.pow(2))

        // сколько прошли: либо расстояние пройденное за тик, либо оставшееся до конечной точки. что меньше
        val distance = (deltaTime * speed).coerceAtMost(td)
        logger.warn("calcNewPoint $deltaTime $distance")

        // помножим расстояние которое должны пройти на единичный вектор
        return if (td == 0.0) {
            Pair(x, y)
        } else
            Pair((x + (tdx / td) * distance).roundToInt(), (y + (tdy / td) * distance).roundToInt())
    }
}