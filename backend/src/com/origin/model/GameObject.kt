package com.origin.model

import com.origin.entity.EntityPosition
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

@ObsoleteCoroutinesApi
sealed class GameObjectMsg {
    class Spawn(val resp: CompletableDeferred<Boolean>)
    class Remove(job: CompletableJob? = null) : MessageWithJob(job)
    class OnRemoved
    class OnObjectRemoved(val obj: GameObject)
    class OnObjectAdded(val obj: GameObject)
}

/**
 * базовый игровой объект в игровой механике
 * все игровые сущности наследуются от него
 */
@ObsoleteCoroutinesApi
open class GameObject(entityPosition: EntityPosition) {
    companion object {
        val logger = LoggerFactory.getLogger(GameObject::class.java)
    }

    /**
     * координаты кэшируем в объекте (потом периодически обновляем в сущности)
     */
    val pos: Position = Position(entityPosition.x,
        entityPosition.y,
        entityPosition.level,
        entityPosition.region,
        entityPosition.heading,
        this)

    protected val actor = CoroutineScope(Dispatchers.IO).actor<Any>(capacity = ACTOR_CAPACITY) {
        channel.consumeEach {
            processMessages(it)
        }
        logger.warn("game obj actor $this finished")
    }

    /**
     * послать сообщение (используя только его класс) с "работой" и ожидать его завершения
     */
    suspend fun sendJobAndJoin(c: KClass<out MessageWithJob>) {
        // ищем конструктор с 1 параметром CompletableJob
        val constructor = c.constructors.singleOrNull {
            it.parameters.size == 1 && it.parameters[0].type.classifier == CompletableJob::class
        } ?: throw RuntimeException("No job constructor")
        // создаем сообщение с новой "работой"
        val msg = constructor.call(Job())
        // шлем сообщение и ждем ответа
        sendJob(msg).join()
    }

    /**
     * отправить сообещине с "работой" и ожидать его завершения
     */
    private suspend fun sendJob(msg: MessageWithJob): CompletableJob {
        assert(msg.job != null)
        actor.send(msg)
        return msg.job!!
    }

    suspend fun send(msg: Any) {
        actor.send(msg)
    }

    protected open suspend fun processMessages(msg: Any) {
        logger.warn("gameobject processMessages ${msg.javaClass.simpleName}")
        when (msg) {
            is GameObjectMsg.Spawn -> msg.resp.complete(pos.spawn())
            is GameObjectMsg.Remove -> {
                remove()
                msg.job?.complete()
            }
            is GameObjectMsg.OnRemoved -> onRemoved()
            is GameObjectMsg.OnObjectRemoved -> onObjectRemoved(msg.obj)
            is GameObjectMsg.OnObjectAdded -> onObjectAdded(msg.obj)

            else -> throw RuntimeException("unprocessed actor message ${msg.javaClass.simpleName}")
        }
    }

    /**
     * текущий активный грид в котором находится объект
     */
    protected val grid: Grid get() = pos.grid

    /**
     * объект который несем над собой, или в котором едем. по сути это контейнер для вложенных объектов
     * они больше не находятся в гриде, а обслуживаются только объектом который их "несет/везет"
     * причем такое состояние только в рантайме. в базе все хранится по координатам. и при рестарте сервера
     * все будет спавнится в одни и теже координаты
     */
    private val lift = ConcurrentHashMap<Int, GameObject>()

    /**
     * удалить объект из мира
     */
    private suspend fun remove() {
        grid.sendJob(GridMsg.RemoveObject(this, Job()))
            .join()
//            .invokeOnCompletion {
        // если есть что-то вложенное внутри
        if (!lift.isEmpty()) {
            lift.values.forEach {
                // TODO
//                    it.pos.set xy coord
                // spawn it
                //it.pos.spawn()
                // store pos into db
            }
//            }
        }
    }

    /**
     * когда ЭТОТ объект удален из грида
     */
    private fun onRemoved() {
        logger.warn("onRemoved")
        // TODO known list
    }

    /**
     * ДРУГОЙ добавили объект в грид в котором находится объект
     */
    open fun onObjectAdded(obj: GameObject) {
        // TODO known list
    }

    /**
     * грид говорит что ДРУГОЙ объект был удален
     */
    open fun onObjectRemoved(obj: GameObject) {
        // TODO known list
    }
}