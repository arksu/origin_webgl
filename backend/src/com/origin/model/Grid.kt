package com.origin.model

import com.origin.TimeController
import com.origin.TimeController.GRID_UPDATE_PERIOD
import com.origin.database.dbQueryCoroutine
import com.origin.entity.EntityObject
import com.origin.entity.EntityObjects
import com.origin.entity.GridEntity
import com.origin.entity.Grids
import com.origin.model.GameObjectMsg.OnObjectAdded
import com.origin.model.GameObjectMsg.OnObjectRemoved
import com.origin.model.move.*
import com.origin.model.objects.ObjectsFactory
import com.origin.net.model.MapGridData
import com.origin.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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
    class SpawnForce(val obj: GameObject) : GridMsg()
    class Activate(val human: Human, job: CompletableJob? = null) : MessageWithJob(job)
    class Deactivate(val human: Human, job: CompletableJob? = null) : MessageWithJob(job)
    class RemoveObject(val obj: GameObject, job: CompletableJob? = null) : MessageWithJob(job)
    class SetTile(val pos: Position, val tile: Byte, job: CompletableJob? = null) : MessageWithJob(job)
    class CheckCollision(
        val obj: GameObject,
        val toX: Int,
        val toY: Int,
        val dist: Double,
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
        val dist: Double,
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

    /**
     * отправить всем активным объектам сообщение
     */
    suspend fun broadcast(msg: BroadcastEvent) {
        actor.send(GridMsg.Broadcast(msg))
    }

    private suspend fun processMessage(msg: Any) {
        when (msg) {
            is GridMsg.Spawn -> msg.resp.complete(spawn(msg.obj, false))
            is GridMsg.SpawnForce -> spawn(msg.obj, true)
            is GridMsg.CheckCollision -> msg.resp.complete(
                checkCollision(
                    msg.obj,
                    msg.toX,
                    msg.toY,
                    msg.dist,
                    msg.type,
                    msg.virtual,
                    msg.isMove
                )
            )
            is GridMsg.CheckCollisionInternal -> msg.resp.complete(
                checkCollisionInternal(
                    msg.list,
                    msg.locked,
                    msg.obj,
                    msg.toX,
                    msg.toY,
                    msg.dist,
                    msg.type,
                    msg.virtual,
                    msg.isMove
                )
            )
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
                        it.session.send(MapGridData(this, MapGridData.Type.CHANGE))
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
                val o = ObjectsFactory.byEntity(it)
                o.pos.setGrid(this@Grid)
                objects.add(o)
            }
        }
    }

    /**
     * спавн объекта в грид в указанную позицию объекта
     * @param force принудительный спавн без каких либо проверок
     */
    private suspend fun spawn(obj: GameObject, force: Boolean): CollisionResult {
        if (obj.pos.region != region || obj.pos.level != level) {
            throw RuntimeException("wrong spawn condition")
        }
        logger.debug("spawn obj grid=${obj.pos.gridX} ${obj.pos.gridY} pos=$x $y")
        if (obj.pos.gridX != x || obj.pos.gridY != y) {
            throw RuntimeException("wrong spawn condition")
        }

        if (force) {
            addObject(obj)
            return CollisionResult.NONE
        } else {
            // в любом случае обновим грид до начала проверок коллизий
            update()

            logger.debug("spawn obj ${obj.pos}")

            // проверим коллизию с объектами и тайлами грида
            val collision = checkCollision(obj, obj.pos.x, obj.pos.y, 0.0, MoveType.SPAWN, null, false)

            if (collision.result == CollisionResult.CollisionType.COLLISION_NONE) {
                addObject(obj)
            }
            return collision
        }
    }

    /**
     * обновление состояния грида и его объектов
     */
    private fun update() {
        // не даем слишком часто апдейтить грид
        if (TimeController.tickCount - lastTick < GRID_UPDATE_PERIOD - 2 && lastTick != 0L) return
        val old = lastTick
        // сразу меняем последний тик
        lastTick = TimeController.tickCount

        // если еще никогда не апдейтили грид - это первичная инициализация, запустим генерацию объектов в гриде
        if (old == 0L) {
            generateObjects()
        } else {
            // TODO grid update
        }

        // обновим в базе время апдейта грида
        dbQueryCoroutine {
            Grids.update({ (Grids.x eq x) and (Grids.y eq y) and (Grids.region eq region) and (Grids.level eq level) }) {
                it[lastTick] = this@Grid.lastTick
            }
        }
    }

    /**
     * первичная генерация объектов в гриде
     */
    private fun generateObjects() {
        // идем по каждому тайлу в гриде
        for (x in 0 until GRID_SIZE) for (y in 0 until GRID_SIZE) {
            val ox = this.x * GRID_FULL_SIZE + (TILE_SIZE / 2)
            val oy = this.y * GRID_FULL_SIZE + (TILE_SIZE / 2)
            val pos = PositionData(x * TILE_SIZE + ox, y * TILE_SIZE + oy, this)
            when (tilesBlob[x + y * GRID_SIZE]) {
                Tile.FOREST_LEAF -> {
                    if (Rnd.next(170) == 0) generateObject(1, pos)
                    if (Rnd.next(320) == 0) generateObject(5, pos)
                }
                Tile.FOREST_PINE -> {
                    if (Rnd.next(130) == 0) generateObject(2, pos)
                    if (Rnd.next(270) == 0) generateObject(3, pos)
                }
            }
        }
    }

    private fun generateObject(type: Int, pos: PositionData) {
        val thisGrid = this
        // запускаем генерацию объекта в корутине
        // вот тут просиходит ай-ай-ай мы в отдельном потоке запускаем генерацию объектов
        WorkerScope.launch {
            val obj = transaction {
                val e = EntityObject.makeNew(pos, type)
                ObjectsFactory.byEntity(e)
            }
            obj.pos.setGrid(thisGrid)
            // шлем сообщение самому себе на спавн объекта
            thisGrid.send(GridMsg.SpawnForce(obj))
        }
    }

    /**
     * проверить коллизию
     */
    private suspend fun checkCollision(
        obj: GameObject,
        toX: Int,
        toY: Int,
        dist: Double,
        moveType: MoveType,
        virtual: GameObject?,
        isMove: Boolean,
    ): CollisionResult {
        // посмотрим сколько нам нужно гридов для проверки коллизий
        val totalDist = obj.pos.point.dist(toX, toY)
        val k = if (totalDist == 0.0) 0.0 else (dist * 1.2) / totalDist
        // точка куда должны передвинутся с учетом вектора движения
        val dp = Vec2i(toX, toY).sub(obj.pos.point).mul(k)

        // область движения (от начальной до конечной точки)
        val rect = obj.getBoundRect().clone().move(obj.pos.x, obj.pos.y).extend(dp.x, dp.y)

        rect.move(obj.getBoundRect())
        val grids = LinkedHashSet<Grid>(4)

        fun addGrid(gx: Int, gy: Int) {
            if (layer.validateCoord(gx, gy)) grids.add(World.getGrid(region, level, gx, gy))
        }

        // добавляем гриды которые захватываются движением
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
        return checkCollisionInternal(list, locked, obj, toX, toY, dist, moveType, virtual, isMove)
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
        dist: Double,
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
            next.send(
                GridMsg.CheckCollisionInternal(
                    list,
                    locked,
                    obj,
                    toX,
                    toY,
                    dist,
                    moveType,
                    virtual,
                    isMove,
                    resp
                )
            )
            resp.await()
        } else {
            // таким образом на момент обработки коллизии
            // все эти гриды будет заблокированы обработкой сообщения обсчета коллизии
            Collision.process(toX, toY, dist, obj, moveType, list, isMove)
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
                human.session.send(MapGridData(this, MapGridData.Type.ADD))
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
            human.session.send(MapGridData(this, MapGridData.Type.REMOVE))
        }
        if (!isActive) {
            TimeController.removeActiveGrid(this)
        }
    }
}
