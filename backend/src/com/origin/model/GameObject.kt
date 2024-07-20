@file:OptIn(ObsoleteCoroutinesApi::class)

package com.origin.model

import com.origin.GRID_FULL_SIZE
import com.origin.ObjectID
import com.origin.TILE_SIZE
import com.origin.model.inventory.Inventory
import com.origin.move.Collision
import com.origin.move.MovingObject
import com.origin.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class GameObject(val id: ObjectID, val pos: ObjectPosition) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(GameObject::class.java)
    }

    /**
     * в каком гриде сейчас находится объект
     * грид указан только тогда, когда берет на себя ответственность за него (заспавнен)
     */
    private var grid: Grid? = null

    val isSpawned: Boolean get() = grid != null

    /**
     * инвентарь объекта (обязательно надо уточнить в конечных объектах, что делать с инвентарем: либо есть либо нет - null)
     */
    open val inventory: Inventory? = null

    /**
     * актор для обработки сообщений
     */
    private val actor by lazy {
        CoroutineScope(ACTOR_DISPATCHER).actor(capacity = ACTOR_BUFFER_CAPACITY) {
            channel.consumeEach { message ->
                try {
                    processMessage(message)
                } catch (t: Throwable) {
                    logger.error("error while process game object message: ${t.message}", t)
                }
            }
            logger.warn("game obj actor $this finished")
        }
    }

    protected open suspend fun processMessage(msg: Any) {
        when (msg) {
            is GameObjectMessage.Spawn -> msg.run { onSpawn(msg.variants) }
            is GameObjectMessage.GridObjectAdded -> onGridObjectAdded(msg.obj)
            is GameObjectMessage.GridObjectRemoved -> onGridObjectRemoved(msg.obj)
            is GameObjectMessage.RemovedFromGrid -> onRemovedFromGrid()
            else -> throw RuntimeException("unprocessed actor message ${msg.javaClass.simpleName} $msg")
        }
    }

    /**
     * отправить сообщение объекту не дожидаясь ответа
     */
    suspend fun send(msg: Any) {
        actor.send(msg)
    }

    suspend fun <T> sendAndWaitAck(msg: MessageWithAck<T>): T {
        actor.send(msg)
        return msg.ack.await()
    }

    /**
     * пытаемя заспавнить объект в мир различными способами
     */
    private suspend fun onSpawn(variants: List<SpawnType>): Boolean {
        for (variant in variants) {
            val result = when (variant) {
                SpawnType.EXACTLY_POINT -> spawn()
                SpawnType.NEAR -> spawnNear()
                SpawnType.RANDOM_SAME_REGION -> spawnRandom()
            }
            if (result) return true
        }
        return false
    }

    /**
     * заспавниться рядом.
     * если удалось - координаты изменятся
     * если не удалось координаты останутся оригинальные.
     */
    private suspend fun spawnNear(): Boolean {
        val origX = pos.point.x
        val origY = pos.point.y

        val len = 2 * TILE_SIZE
        for (t in 0 until 10) {
            var dx = Rnd.next(len * 2) - len
            var dy = Rnd.next(len * 2) - len

            if (dx < 0 && dx > -TILE_SIZE) dx -= TILE_SIZE
            if (dx > 0 && dx < TILE_SIZE) dx += TILE_SIZE
            if (dy < 0 && dy > -TILE_SIZE) dy -= TILE_SIZE
            if (dy > 0 && dy < TILE_SIZE) dy += TILE_SIZE

            pos.point.x = origX + dx
            pos.point.y = origY + dy

            logger.debug("try spawn near {}", pos)
            if (spawn()) {
                return true
            }
        }

        pos.point.x = origX
        pos.point.y = origY
        logger.debug("spawn failed")
        return false
    }

    /**
     * заспавнится в случайной точке текущего слоя
     * если удалось - координаты изменятся
     * если не удалось координаты останутся оригинальные.
     */
    private suspend fun spawnRandom(): Boolean {
        val origX = pos.point.x
        val origY = pos.point.y

        val layer = World.getRegion(pos.region).getLayer(pos.level)
        val border = GRID_FULL_SIZE + Collision.WORLD_BUFFER_SIZE
        val rangeW = layer.width * GRID_FULL_SIZE - border * 2
        val rangeH = layer.height * GRID_FULL_SIZE - border * 2

        for (t in 0 until 10) {
            pos.point.x = Rnd.next(rangeW) + border
            pos.point.y = Rnd.next(rangeH) + border

            logger.debug("try spawn random {}", pos)
            if (spawn()) {
                return true
            }
        }

        pos.point.x = origX
        pos.point.y = origY
        logger.debug("spawn failed")
        return false
    }

    private suspend fun spawn(): Boolean {
        if (isSpawned) throw IllegalStateException("grid is already set, on spawn")

        // берем грид и спавнимся через него
        val g = World.getGrid(pos)

        val spawned = g.sendAndWaitAck(GridMessage.Spawn(this))

        // если успешно добавились в грид - запомним его у себя
        return if (spawned) {
            grid = g
            afterSpawn()
            true
        } else {
            false
        }
    }

    protected open suspend fun afterSpawn() {
    }

    suspend fun setXY(x: Int, y: Int) {
        logger.debug("setXY $x $y")

        // запомним координаты старого грида
        val oldGx = pos.gridX
        val oldGy = pos.gridY

        // поставим новые координаты
        pos.point.x = x
        pos.point.y = y

        if (isSpawned) {
            // если координаты грида изменились
            if (oldGx != pos.gridX || oldGy != pos.gridY) {
                val old = grid ?: throw IllegalStateException("old grid is null")
                // получим новый грид из мира
                val newGrid = World.getGrid(pos)
                grid = newGrid
                if (this is MovingObject) {
                    // уведомим объект о смене грида
                    onGridChanged()
                }
                old.objects.remove(this)
                newGrid.objects.add(this)
            }
        }
    }

    /**
     * удалить объект из мира, это последнее, что может сделать объект
     * после этого его актор убивается
     */
    protected open suspend fun remove() {
        // учесть телепорт когда грида еще еще нет
        grid?.sendAndWait(GridMessage.RemoveObject(this))

        // если есть что-то вложенное внутри
        // TODO
//        if (!lift.isEmpty()) {
//            lift.values.forEach { _ ->
//                // TODO remove when lift it.pos.set xy coord
//                // spawn it
//                // it.pos.spawn()
//                // store pos into db
//            }
//        }
        // завершаем актора
        actor.close()
    }

    abstract fun save()

    /**
     * когда ЭТОТ объект удален из грида
     */
    protected open suspend fun onRemovedFromGrid() {
        logger.warn("onRemoved")
    }

    /**
     * ДРУГОЙ добавили объект в грид в котором находится объект
     */
    open suspend fun onGridObjectAdded(obj: GameObject) {
    }

    /**
     * грид говорит что ДРУГОЙ объект был удален
     */
    protected open suspend fun onGridObjectRemoved(obj: GameObject) {
    }

    abstract fun getBoundRect(): Rect
    abstract fun getResourcePath(): String

    /**
     * игрок вызывает контекстное меню объекта
     * выполняется в контексте ИГРОКА
     */
    open fun openContextMenu(p: Player): ContextMenu? {
        // по дефолту у объектов нет контекстного меню
        return null
    }

    /**
     * обработать выбор контекстного меню игроком
     * выполняется в контексте ИГРОКА
     */
    open suspend fun executeContextMenuItem(player: Player, item: String) {
    }

    /**
     * дает ли коллизию этот объект с другим (в качестве парметра передаем объект, который движется)
     * упрется ли движущийся объект в этот (в меня)
     */
    open fun isCollideWith(moving: GameObject): Boolean {
        return true
    }

    fun getGridSafety(): Grid {
        return grid ?: throw IllegalStateException("grid is null")
    }

    fun setGrid(g: Grid) {
        this.grid = g
    }

    open fun afterLoad() {
    }

    open fun getHP(): Int {
        return 0
    }

    open fun setHP(hp: Int) {
    }

    override fun toString(): String {
        return "${this::class.simpleName} [$id] ${pos.point}"
    }
}