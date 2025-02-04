@file:OptIn(ObsoleteCoroutinesApi::class)

package com.origin.model

import com.origin.*
import com.origin.TimeController.GRID_UPDATE_PERIOD
import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.records.GridRecord
import com.origin.jooq.tables.records.ObjectRecord
import com.origin.jooq.tables.references.GRID
import com.origin.jooq.tables.references.OBJECT
import com.origin.model.`object`.ObjectsFactory
import com.origin.model.`object`.Boulder
import com.origin.model.`object`.tree.Apple
import com.origin.model.`object`.tree.Birch
import com.origin.model.`object`.tree.Fir
import com.origin.model.`object`.tree.Pine
import com.origin.move.CheckCollisionModel
import com.origin.move.Collision
import com.origin.move.CollisionResult
import com.origin.move.MoveType
import com.origin.net.MapGridData
import com.origin.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentLinkedQueue

class Grid(
    private val record: GridRecord,
    val layer: LandLayer,
) {

    val tilesBlob get() = record.tilesBlob

    val pos = Vec2i(record.x, record.y)
    val region get() = record.region
    val level get() = record.level
    val x: Int get() = record.x
    val y: Int get() = record.y

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

    /**
     * актор для обработки сообщений
     */
    private val actor = CoroutineScope(ACTOR_DISPATCHER).actor(capacity = ACTOR_BUFFER_CAPACITY) {
        channel.consumeEach {
            try {
                processMessage(it)
            } catch (t: Throwable) {
                logger.error("error while process grid message: ${t.message}", t)
            }
        }
        logger.warn("game obj actor $this finished")
    }

    init {
        loadObjects()
    }

    private suspend fun processMessage(msg: Any) {
        when (msg) {
            is GridMessage.Update -> update()
            is GridMessage.Spawn -> msg.run { onSpawn(msg.obj, false) }
            is GridMessage.SpawnForce -> onSpawn(msg.obj, true)
            is GridMessage.RemoveObject -> msg.run { onRemoveObject(msg.obj) }
            is GridMessage.Activate -> msg.run { onActivate(msg.obj) }
            is GridMessage.Deactivate -> msg.run { onDeactivate(msg.obj) }
            is GridMessage.CheckCollisionInternal -> msg.run { checkCollisionInternal(msg.model) }
            is GridMessage.CheckCollision -> msg.run {
                checkCollision(
                    msg.obj,
                    msg.toX,
                    msg.toY,
                    msg.dist,
                    msg.type,
                    msg.virtual,
                    msg.isMove
                )
            }

            is GridMessage.Broadcast -> activeObjects.forEach {
                it.send(msg.e)
            }

            else -> logger.error("Unknown Grid message $msg")
        }
    }

    suspend fun send(msg: Any) {
        actor.send(msg)
    }

    suspend fun <T> sendAndWaitAck(msg: MessageWithAck<T>): T {
        actor.send(msg)
        return msg.ack.await()
    }

    suspend fun sendAndWait(msg: MessageWithJob) {
        actor.send(msg)
        return msg.job.join()
    }

    /**
     * отправить всем активным объектам сообщение
     */
    suspend fun broadcast(msg: BroadcastEvent) {
        actor.send(GridMessage.Broadcast(msg))
    }

    private suspend fun onSpawn(obj: GameObject, force: Boolean): Boolean {
        if (obj.pos.region != region || obj.pos.level != level) {
            throw RuntimeException("wrong spawn condition")
        }
//        logger.debug("spawn obj grid=${obj.pos.gridX} ${obj.pos.gridY}")
        if (obj.pos.gridX != x || obj.pos.gridY != y) {
            throw RuntimeException("wrong spawn condition")
        }

        if (force) {
            addObject(obj)
            return true
        } else {
            // в любом случае обновим грид до начала проверок коллизий
            update()

            logger.debug("spawn obj {}", obj.pos)

            // проверим коллизию с объектами и тайлами грида
            val collision = checkCollision(obj, obj.pos.x, obj.pos.y, 0.0, MoveType.SPAWN, null, false)

            if (collision.result == CollisionResult.CollisionType.COLLISION_NONE) {
                addObject(obj)
                return true
            }
            return false
        }
    }

    /**
     * добавить объект в грид
     */
    private suspend fun addObject(obj: GameObject) {
        if (!objects.contains(obj)) {
            objects.add(obj)
            obj.setGrid(this)

            activeObjects.forEach {
                it.send(GameObjectMessage.GridObjectAdded(obj))
            }
        }
    }

    /**
     * удалить объект из грида
     */
    private suspend fun onRemoveObject(obj: GameObject) {
        if (objects.remove(obj)) {
            obj.send(GameObjectMessage.RemovedFromGrid())

            activeObjects.forEach {
                it.send(GameObjectMessage.GridObjectRemoved(obj))
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
    private suspend fun onActivate(human: Human) {
        if (!activeObjects.contains(human)) {
            // если грид был до этого не активен обязательно надо обновить состояние
            if (!isActive) {
                update()
            }

            activeObjects.add(human)

            TimeController.addActiveGrid(this)
        }
        if (human is Player) {
            human.sendToSocket(MapGridData(this, MapGridData.Type.ADD))
        }
    }

    /**
     * деактивировать грид
     * если в гриде не осталось ни одного активного объекта то он прекращает обновляться
     */
    private suspend fun onDeactivate(human: Human) {
        activeObjects.remove(human)
        if (human is Player) {
            human.sendToSocket(MapGridData(this, MapGridData.Type.REMOVE))
        }
        if (!isActive) {
            TimeController.removeActiveGrid(this)
        }
    }

    fun updateTiles() {
        val affected = DatabaseConfig.dsl
            .update(GRID)
            .set(GRID.TILES_BLOB, record.tilesBlob)
            .where(GRID.X.eq(record.x))
            .and(GRID.Y.eq(record.y))
            .and(GRID.REGION.eq(record.region))
            .and(GRID.LEVEL.eq(record.level))
            .execute()
        if (affected != 1) throw RuntimeException("failed update grid $record tiles")
    }

    /**
     * обновление состояния грида и его объектов
     */
    private fun update() {
        // не даем слишком часто апдейтить грид
        if (TimeController.tickCount - record.lastTick < GRID_UPDATE_PERIOD - 2 && record.lastTick != 0L) return
        val oldLastTick = record.lastTick
        // сразу меняем последний тик
        record.lastTick = TimeController.tickCount

        // если еще никогда не апдейтили грид - это первичная инициализация, запустим генерацию объектов в гриде
        if (oldLastTick == 0L) {
            generateObjects()
        } else {
            // TODO grid update
        }

        // обновим в базе время апдейта грида
        DatabaseConfig.dsl
            .update(GRID)
            .set(GRID.LAST_TICK, record.lastTick)
            .where(GRID.X.eq(record.x))
            .and(GRID.Y.eq(record.y))
            .and(GRID.REGION.eq(record.region))
            .and(GRID.LEVEL.eq(record.level))
            .execute()
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

        val list = grids.toMutableList()
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
        return checkCollisionInternal(
            CheckCollisionModel(list, locked, obj, toX, toY, dist, moveType, virtual, isMove)
        )
    }

    /**
     * внутренняя обработка коллизии на заблокированных гридах
     */
    private suspend fun checkCollisionInternal(
        model: CheckCollisionModel
    ): CollisionResult {
        val current = model.list[model.locked.size]
        model.locked.add(current)

        // последний получивший и обработает коллизию вернет результат в deferred и остальные сделают также
        return if (model.locked.size < model.list.size) {
            val next = model.list[model.locked.size]
            logger.warn("delegate collision to next $next")
            next.sendAndWaitAck(GridMessage.CheckCollisionInternal(model))
        } else {
            // таким образом на момент обработки коллизии
            // все эти гриды будет заблокированы обработкой сообщения обсчета коллизии
            Collision.process(model.toX, model.toY, model.dist, model.obj, model.moveType, model.list, model.isMove)
        }
    }

    /**
     * загрузка объектов при создании грида
     */
    private fun loadObjects() {
        val list: List<ObjectRecord> = DatabaseConfig.dsl.selectFrom(OBJECT)
            .where(OBJECT.GRID_X.eq(x))
            .and(OBJECT.GRID_Y.eq(y))
            .and(OBJECT.REGION.eq(region))
            .and(OBJECT.LEVEL.eq(level))
            .fetch()
        list.forEach { record ->
            val obj = ObjectsFactory.create(record)
            obj.setGrid(this)
            objects.add(obj)
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
            val pos = ObjectPosition(x * TILE_SIZE + ox, y * TILE_SIZE + oy, this.level, this.region, 0)
            when (tilesBlob[x + y * GRID_SIZE]) {
                Tile.FOREST_LEAF -> {
                    if (Rnd.next(170) == 0) generateObject(Birch::class.java, pos)
                    if (Rnd.next(320) == 0) generateObject(Apple::class.java, pos)
                    if (Rnd.next(450) == 0) generateObject(Boulder::class.java, pos)
                }

                Tile.FOREST_PINE -> {
                    if (Rnd.next(130) == 0) generateObject(Fir::class.java, pos)
                    if (Rnd.next(270) == 0) generateObject(Pine::class.java, pos)
                    if (Rnd.next(450) == 0) generateObject(Boulder::class.java, pos)
                }
            }
        }
    }

    fun generateObject(clazz: Class<*>, pos: ObjectPosition, data: String? = null) {
        // запускаем генерацию объекта в корутине
        // вот тут просиходит ай-ай-ай мы в отдельном потоке запускаем генерацию объектов
        WorkerScope.launch {
            @Suppress("UNCHECKED_CAST")
            val obj = ObjectsFactory.create(clazz as Class<GameObject>, pos, data)

            obj.save()
            // шлем сообщение самому себе на спавн объекта
            // т.к. мы сейчас в корутине
            this@Grid.send(GridMessage.SpawnForce(obj))
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Grid::class.java)

        /**
         * загрузить грид из базы
         */
        fun load(gx: Int, gy: Int, layer: LandLayer): Grid {
            val grid = DatabaseConfig.dsl
                .selectFrom(GRID)
                .where(GRID.X.eq(gx))
                .and(GRID.Y.eq(gy))
                .and(GRID.LEVEL.eq(layer.level))
                .and(GRID.REGION.eq(layer.region.id))
                .fetchOne() ?: throw RuntimeException("grid ($gx, $gy) level=${layer.level} region=${layer.region.id} is not found")
            return Grid(grid, layer)
        }
    }

}