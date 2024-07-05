@file:OptIn(ObsoleteCoroutinesApi::class)

package com.origin.model

import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.records.GridRecord
import com.origin.jooq.tables.references.GRID
import com.origin.move.CollisionResult
import com.origin.util.ACTOR_BUFFER_CAPACITY
import com.origin.util.ACTOR_DISPATCHER
import com.origin.util.MessageWithAck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Grid(
    private val record: GridRecord,
    val layer: LandLayer,
) {
    val tilesBlob get() = record.tilesBlob

    val x: Int get() = record.x

    val y: Int get() = record.y

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

    suspend fun <T> sendAndWaitAck(msg: MessageWithAck<T>): T {
        actor.send(msg)
        return msg.ack.await()
    }

    private suspend fun processMessage(msg: Any) {
        when (msg) {
            is GridMessage.Spawn -> msg.run { onSpawn(msg.obj) }

            else -> logger.error("Unknown Grid message $msg")
        }
    }

    private fun onSpawn(obj: GameObject): CollisionResult {
//        throw RuntimeException("123")
        TODO("Not yet implemented")
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

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Grid::class.java)

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