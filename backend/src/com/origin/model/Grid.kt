package com.origin.model

import com.origin.entity.GridEntity
import com.origin.net.model.GameResponse
import com.origin.net.model.MapGridData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import org.jetbrains.exposed.sql.ResultRow
import java.util.concurrent.ConcurrentLinkedQueue

@ObsoleteCoroutinesApi
sealed class GridMsg {
    class Spawn(val obj: GameObject, val resp: CompletableDeferred<CollisionResult>) : GridMsg()
    class Activate(val human: Human, job: CompletableJob? = null) : MessageWithJob(job)
    class Deactivate(val human: Human, job: CompletableJob? = null) : MessageWithJob(job)
    class RemoveObject(val obj: GameObject, job: CompletableJob? = null) : MessageWithJob(job)
}

@ObsoleteCoroutinesApi
class Grid(r: ResultRow, l: LandLayer) : GridEntity(r, l) {
    private val actor = CoroutineScope(Dispatchers.IO).actor<Any>(capacity = ACTOR_CAPACITY) {
        channel.consumeEach {
            processMessages(it)
        }
    }

    suspend fun sendJob(msg: MessageWithJob): CompletableJob {
        assert(msg.job != null)
        actor.send(msg)
        return msg.job!!
    }

    suspend fun send(msg: Any) {
        actor.send(msg)
    }

    private suspend fun processMessages(msg: Any) {
        logger.debug("grid processMessages ${msg.javaClass.simpleName}")
        when (msg) {
            is GridMsg.Spawn -> msg.resp.complete(spawn(msg.obj))
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
        }
    }

    /**
     * список активных объектов которые поддерживают этот грид активным
     * также всем активным объектам рассылаем уведомления о том что происходит в гриде (события)
     */
    private val activeObjects = ConcurrentLinkedQueue<Human>()

    /**
     * список объектов в гриде
     */
    private val objects = ConcurrentLinkedQueue<GameObject>()

    /**
     * активен ли грид?
     */
    private val isActive: Boolean get() = !activeObjects.isEmpty()

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

        // проверим коллизию с объектами и тайлами грида
        val collision = checkCollsion(obj, obj.pos.x, obj.pos.y, obj.pos.x, obj.pos.y, MoveType.SPAWN)

        if (collision.result == CollisionResult.CollisionType.COLLISION_NONE) {
            addObject(obj)
        }
        return collision
    }

    /**
     * обновление состояния грида и его объектов
     */
    private fun update() {

    }

    /**
     * проверить коллизию
     */
    private fun checkCollsion(
        obj: GameObject,
        x: Int,
        y: Int,
        toX: Int,
        toY: Int,
        moveType: MoveType,
    ): CollisionResult {

        // TODO
        return CollisionResult.NONE
    }

    /**
     * добавить объект в грид
     * перед вызовом грид обязательно должен быть залочен!!!
     */
    private suspend fun addObject(obj: GameObject) {
        if (!objects.contains(obj)) {
            objects.add(obj)

            if (isActive) activeObjects.forEach {
                it.send(GameObjectMsg.OnObjectAdded(obj))
            }
        }
    }

    /**
     * удалить объект из грида
     */
    private suspend fun removeObject(obj: GameObject) {
        if (objects.contains(obj)) {
            objects.remove(obj)
            obj.send(GameObjectMsg.OnRemoved())

            if (isActive) activeObjects.forEach {
                it.send(GameObjectMsg.OnObjectRemoved(obj))
            }
        }
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
            if (!isActive) {
                update()
            }

            activeObjects.add(human)
            if (human is Player) {
                logger.debug("GameResponse map $x $y")
                human.session.send(GameResponse("map", MapGridData(this)))
            }

            World.instance.addActiveGrid(this)
        }
    }

    /**
     * деактивировать грид
     * если в гриде не осталось ни одного активного объекта то он прекращает обновляться
     */
    private fun deactivate(human: Human) {
        activeObjects.remove(human)

        if (!isActive) {
            World.instance.removeActiveGrid(this)
        }
    }
}