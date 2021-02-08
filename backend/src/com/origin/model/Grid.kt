package com.origin.model

import com.origin.Const
import com.origin.TimeController
import com.origin.collision.CollisionResult
import com.origin.entity.EntityObject
import com.origin.entity.EntityObjects
import com.origin.entity.GridEntity
import com.origin.model.GameObjectMsg.OnObjectAdded
import com.origin.model.GameObjectMsg.OnObjectRemoved
import com.origin.model.move.MoveType
import com.origin.model.move.Position
import com.origin.net.model.MapGridData
import com.origin.utils.GRID_FULL_SIZE
import com.origin.utils.Rect
import com.origin.utils.Vec2i
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentLinkedQueue

@ObsoleteCoroutinesApi
sealed class BroadcastEvent {
    class Moved(
        val obj: GameObject,
        val toX: Int,
        val toY: Int,
        val speed: Double,
        val moveType: MoveType,
    ) : BroadcastEvent()

    class StartMove(
        val obj: GameObject,
        val toX: Int,
        val toY: Int,
        val speed: Double,
        val moveType: MoveType,
    ) : BroadcastEvent()

    class Stopped(val obj: GameObject) : BroadcastEvent()

    class Changed(val obj: GameObject) : BroadcastEvent()

    class ChatMessage(val obj: GameObject, val channel: Int, val text: String) : BroadcastEvent() {
        companion object {
            const val GENERAL = 0
            const val PRIVATE = 1
            const val PARTY = 2
            const val VILLAGE = 3
            const val SHOUT = 4
            const val WORLD = 5
            const val ANNOUNCEMENT = 6
            const val SYSTEM = 0xff
        }
    }
}

@ObsoleteCoroutinesApi
sealed class GridMsg {
    class Spawn(val obj: GameObject, val resp: CompletableDeferred<CollisionResult>) : GridMsg()
    class Activate(val human: Human, job: CompletableJob? = null) : MessageWithJob(job)
    class Deactivate(val human: Human, job: CompletableJob? = null) : MessageWithJob(job)
    class RemoveObject(val obj: GameObject, job: CompletableJob? = null) : MessageWithJob(job)
    class SetTile(val pos: Position, val tile: Byte, job: CompletableJob? = null) : MessageWithJob(job)
    class CheckCollision(
        val obj: GameObject,
        val toX: Int,
        val toY: Int,
        val type: MoveType,
        val virtual: GameObject?,
        val isMove: Boolean,
        val resp: CompletableDeferred<CollisionResult>,
    ) : GridMsg()

    class CheckCollisionInternal(
        val list: Array<Grid>,
        val locked: ArrayList<Grid>,
        val obj: GameObject,
        val toX: Int,
        val toY: Int,
        val type: MoveType,
        val virtual: GameObject?,
        val isMove: Boolean,
        val resp: CompletableDeferred<CollisionResult>,
    ) : GridMsg()

    class Broadcast(val e: BroadcastEvent) : GridMsg()

    class Update
}

@ObsoleteCoroutinesApi
class Grid(r: ResultRow, l: LandLayer) : GridEntity(r, l) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(Grid::class.java)
    }

    val pos = Vec2i(x, y)

    /**
     * список активных объектов которые поддерживают этот грид активным
     * также всем активным объектам рассылаем уведомления о том что происходит в гриде (события)
     */
    private val activeObjects = ConcurrentLinkedQueue<Human>()

    /**
     * список объектов в гриде
     */
    val objects = ConcurrentLinkedQueue<GameObject>()

    /**
     * активен ли грид?
     */
    private val isActive: Boolean get() = !activeObjects.isEmpty()

    init {
        loadObjects()
    }

    /**
     * актор для обработки сообщений
     */
    private val actor = CoroutineScope(ACTOR_DISPATCHER).actor<Any>(capacity = ACTOR_BUFFER_CAPACITY) {
        channel.consumeEach {
            try {
                processMessage(it)
            } catch (t: Throwable) {
                logger.error("error while process grid message: ${t.message}", t)
            }
        }
        logger.warn("grid actor $this finished")
    }

    suspend fun sendJob(msg: MessageWithJob): CompletableJob {
        assert(msg.job != null)
        actor.send(msg)
        return msg.job!!
    }

    suspend fun send(msg: Any) {
        actor.send(msg)
    }

    suspend fun broadcast(msg: BroadcastEvent) {
        actor.send(GridMsg.Broadcast(msg))
    }

    private suspend fun processMessage(msg: Any) {
        when (msg) {
            is GridMsg.Spawn -> msg.resp.complete(spawn(msg.obj))
            is GridMsg.CheckCollision -> msg.resp.complete(checkCollision(
                msg.obj,
                msg.toX,
                msg.toY,
                msg.type,
                msg.virtual,
                msg.isMove))
            is GridMsg.CheckCollisionInternal -> msg.resp.complete(checkCollisionInternal(
                msg.list,
                msg.locked,
                msg.obj,
                msg.toX,
                msg.toY,
                msg.type,
                msg.virtual,
                msg.isMove))
            is GridMsg.Activate -> {
                this.activate(msg.human)
                msg.job?.complete()
            }
            is GridMsg.Deactivate -> {
                this.deactivate(msg.human)
                msg.job?.complete()
            }
            is GridMsg.RemoveObject -> {
                this.removeObject(msg.obj)
                msg.job?.complete()
            }
            is GridMsg.SetTile -> {
                tilesBlob[msg.pos.tileIndex] = msg.tile
                transaction {
                    updateTiles()
                }
                activeObjects.forEach {
                    if (it is Player) {
                        it.session.send(MapGridData(this, 2))
                    }
                }
                msg.job?.complete()
            }
            is GridMsg.Update -> update()
            is GridMsg.Broadcast -> activeObjects.forEach { it.send(msg.e) }
            else -> logger.warn("Unknown Grid message $msg")
        }
    }

    private fun loadObjects() {
        transaction {
            val list =
                EntityObject.find { (EntityObjects.gridx eq x) and (EntityObjects.gridy eq y) and (EntityObjects.region eq region) and (EntityObjects.level eq level) }
            list.forEach {
                val o = Const.getObjectByType(it)
                o.pos.setGrid(this@Grid)
                objects.add(o)
            }
        }
    }

    /**
     * спавн объекта в грид
     */
    private suspend fun spawn(obj: GameObject): CollisionResult {
        if (obj.pos.region != region || obj.pos.level != level ||
            obj.pos.gridX != x || obj.pos.gridY != y
        ) {
            throw RuntimeException("wrong spawn condition")
        }

        // в любом случае обновим грид до начала проверок коллизий
        update()

        logger.debug("spawn obj ${obj.pos}")

        // проверим коллизию с объектами и тайлами грида
        val collision = checkCollision(obj, obj.pos.x, obj.pos.y, MoveType.SPAWN, null, false)

        if (collision.result == CollisionResult.CollisionType.COLLISION_NONE) {
            addObject(obj)
        }
        return collision
    }

    /**
     * обновление состояния грида и его объектов
     */
    private fun update() {
        // TODO grid update
    }

    /**
     * проверить коллизию
     */
    private suspend fun checkCollision(
        obj: GameObject,
        toX: Int,
        toY: Int,
        moveType: MoveType,
        virtual: GameObject?,
        isMove: Boolean,
    ): CollisionResult {
        // посмотрим сколько нам нужно гридов для проверки коллизий
        val rect = Rect(obj.pos.x, obj.pos.y, toX, toY)
        rect.add(obj.getBoundRect())
        val grids = LinkedHashSet<Grid>(4)

        fun addGrid(gx: Int, gy: Int) {
            if (layer.validateCoord(gx, gy)) grids.add(World.getGrid(region, level, gx, gy))
        }

        addGrid(rect.left / GRID_FULL_SIZE, rect.top / GRID_FULL_SIZE)
        addGrid(rect.right / GRID_FULL_SIZE, rect.top / GRID_FULL_SIZE)
        addGrid(rect.right / GRID_FULL_SIZE, rect.bottom / GRID_FULL_SIZE)
        addGrid(rect.left / GRID_FULL_SIZE, rect.bottom / GRID_FULL_SIZE)

        val locked = ArrayList<Grid>(4)
        val list = grids.toTypedArray()

        // если в списке нужных гридов 2 и более
        if (list.size > 1) {
            // ищем себя
            val idx = list.indexOf(this)
            // и ставим в 0 индекс. так чтобы обработка началась с этого грида
            // для того чтобы не слать сообщение checkCollisionInternal самому себе
            if (idx != 0) {
                val temp = list[0]
                list[0] = list[idx]
                list[idx] = temp
            }
        }

        // шлем сообщения всем гридам задетых в коллизии
        return checkCollisionInternal(list, locked, obj, toX, toY, moveType, virtual, isMove)
    }

    /**
     * внутренняя обработка коллизии на заблокированных гридах
     */
    private suspend fun checkCollisionInternal(
        list: Array<Grid>,
        locked: ArrayList<Grid>,
        obj: GameObject,
        toX: Int,
        toY: Int,
        moveType: MoveType,
        virtual: GameObject?,
        isMove: Boolean,
    ): CollisionResult {
        val current = list[locked.size]
        locked.add(current)

        // последний получивший и обработает коллизию вернет результат в deferred и остальные сделают также
        return if (locked.size < list.size) {
            val next = list[locked.size]
            logger.warn("delegate collision to next $next")
            val resp = CompletableDeferred<CollisionResult>()
            next.send(GridMsg.CheckCollisionInternal(list, locked, obj, toX, toY, moveType, virtual, isMove, resp))
            resp.await()
        } else {
            // таким образом на момент обработки коллизии
            // все эти гриды будет заблокированы обработкой сообщения обсчета коллизии

            // определяем вектор движения для отсечения объектов которые находятся за пределами вектора
            val dx = toX - obj.pos.x
            val dy = toY - obj.pos.y
            // прямоугольник по границам объекта захватывающий начальную и конечную точку движения
            val mr = Rect(obj.getBoundRect()).add(obj.pos.point).extend(dx, dy)
            val or = Rect(obj.getBoundRect()).add(obj.pos.point)
//            list.forEach {
//                logger.debug("$it")
//            }

            logger.debug("obj $obj d $dx, $dy move rect $mr")
//            logger.warn("obj rect $or")
            // получаем список объектов для обсчета коллизий из списка гридов
            val filtered = list.flatMap { it ->
                // фильтруем список объектов. вернем только те которые ТОЧНО МОГУТ дать коллизию
                // те которые далеко или не попадают в прямоугольник движения - отсечем их
                it.objects.filter {
                    // сами себе точно не даем коллизию
                    if (obj == it) return@filter false
                    val r = Rect(it.getBoundRect()).add(it.pos.point)
//                    logger.debug("$it $r")
                    // границы объекта должны быть в границах вектора движения объекта
                    // то есть пересекаться с областью движения объекта
                    mr.isIntersect(r)
                }
            }

            logger.warn("filtered:")
            filtered.forEach {
                // ищем минимальное расстояние от движущегося до объекта который может дать коллизию
                val r = Rect(it.getBoundRect()).add(it.pos.point)
                val (mx, my) = or.min(r)
                logger.warn("$it $r min $mx $my")
                // пересечение по обеим осям. значит пересечение объектов уже на старте
                if (mx == -1 && my == -1) {
                    return CollisionResult(CollisionResult.CollisionType.COLLISION_OBJECT, it)
                } else {
                    // если хотябы по одной из осей нет пересечения
                    if (mx >= 0) {
                        // вычислим расстояние до пересечения по другой оси
                        val k: Double = dy.toDouble() / dx.toDouble()
                        val kd = Math.abs(Math.round(k * mx).toInt())
                        val fx = if (dx < 0) -mx else mx
                        val fy = if (dy < 0) -kd else kd
                        // передвинем рект игрока в позицию предполагаемого пересечения
                        val tr = Rect(or).add(fx, fy)
                        logger.warn("temp1 rect $tr k=$k kd=$kd")
                        // вычислим фактическое расстояние между двумя ректами еще раз
                        val (mmx, mmy) = tr.min(r)
                        logger.warn("min $mmx $mmy")
                        // если оно довольно мало считаем что пересечение есть. и это коллизия с объектом
                        if (mmx <= 1 && mmy <= 1) {
//                            logger.warn("collision!")
                            if (isMove && (fx != 0 || fy != 0)) {
                                // немного сдвинем назад чтобы точно был оступ от коллизии в 1 единицу
                                val cx = Math.max(0, mx - 1)
                                obj.pos.setXY(obj.pos.x + (if (dx < 0) -cx else cx), obj.pos.y + fy)
                            }
                            return CollisionResult(CollisionResult.CollisionType.COLLISION_OBJECT, it)
                        }
                    }
                    if (my >= 0) {
                        val k: Double = dx.toDouble() / dy.toDouble()
                        val kd = Math.abs(Math.round(k * my).toInt())
                        val fx = if (dx < 0) -kd else kd
                        val fy = if (dy < 0) -my else my
                        val tr = Rect(or).add(fx, fy)
                        logger.warn("temp2 rect $tr k=$k kd=$kd")
                        val (mmx, mmy) = tr.min(r)
                        logger.warn("min $mmx $mmy")
                        if (mmx <= 1 && mmy <= 1) {
//                            logger.warn("collision!")
                            if (isMove && (fx != 0 || fy != 0)) {
                                val cy = Math.max(0, my - 1)
                                obj.pos.setXY(obj.pos.x + fx, obj.pos.y + (if (dy < 0) -cy else cy))
                            }
                            return CollisionResult(CollisionResult.CollisionType.COLLISION_OBJECT, it)
                        }
                    }
                }
            }

            if (isMove) {
                obj.pos.setXY(toX, toY)
            }
            CollisionResult.NONE
        }
    }

    /**
     * добавить объект в грид
     */
    private suspend fun addObject(obj: GameObject) {
        if (!objects.contains(obj)) {
            objects.add(obj)

            if (isActive) activeObjects.forEach {
                it.send(OnObjectAdded(obj))
            }
        }
    }

    /**
     * удалить объект из грида
     */
    private suspend fun removeObject(obj: GameObject) {
        if (objects.remove(obj)) {
            obj.send(GameObjectMsg.OnRemoved())

            if (isActive) activeObjects.forEach {
                it.send(OnObjectRemoved(obj))
            }
        }
        logger.debug("objects size=${objects.size}")
    }

    /**
     * активировать грид
     * только пока есть хоть 1 объект связанный с гридом - он будет считатся активным
     * если ни одного объекта нет грид становится не активным и не обновляет свое состояние
     * @param human объект который связывается с гридом
     * @return только если удалось активировать
     */
    private suspend fun activate(human: Human) {
        if (!activeObjects.contains(human)) {
            // если грид был до этого не активен обязательно надо обновить состояние
            if (!isActive) {
                update()
            }

            activeObjects.add(human)
            if (human is Player) {
                human.session.send(MapGridData(this, 1))
            }

            TimeController.addActiveGrid(this)
        }
    }

    /**
     * деактивировать грид
     * если в гриде не осталось ни одного активного объекта то он прекращает обновляться
     */
    private suspend fun deactivate(human: Human) {
        activeObjects.remove(human)
        if (human is Player) {
            human.session.send(MapGridData(this, 0))
        }
        if (!isActive) {
            TimeController.removeActiveGrid(this)
        }
    }
}