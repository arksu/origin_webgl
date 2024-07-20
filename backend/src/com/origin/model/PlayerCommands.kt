package com.origin.model

import com.origin.model.inventory.ItemType
import com.origin.model.`object`.ObjectsFactory
import com.origin.move.PositionModel
import kotlinx.coroutines.CompletableDeferred
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object PlayerCommands {
    private val logger: Logger = LoggerFactory.getLogger(PlayerCommands::class.java)

    suspend fun runCommand(player: Player, cmd: String) {
        logger.warn("adminCommand $cmd")
        val params = cmd.split(" ")
        if (params.isEmpty()) return
        when (params[0]) {
            "give" -> {
                // param 1 - type id || name type
                val typeId: Int = params[1].toIntOrNull() ?: ItemType.fromName(params[1].lowercase()).id

                val newItem = ObjectsFactory.createInventoryItem(typeId)
                if (!player.inventory.putItem(newItem)) {
                    // TODO new item drop to ground
                }
            }

            "spawn" -> {
                player.commandToExecuteByMapClick = cmd
            }

            else -> {
//                player.session.send(CreatureSay(0, text, SYSTEM))("Unknown command: $cmd")
            }
        }
    }

    suspend fun runCommandByMapClick(player: Player, cmd: String, x: Int, y: Int) {
        logger.warn("runCommandByMapClick $cmd")

        val params = cmd.split(" ")
        if (params.isEmpty()) return

        when (params[0]) {
            "spawn" -> {
                // param 1 - type id
                val t: Int = params[1].toInt()
                // param 2 - data for object
                val d = if (params.size >= 3) params[2] else null
                val posModel = PositionModel(x, y, player.pos)
                val record = ObjectsFactory.createAndInsert(t, posModel)
                val newObject = ObjectsFactory.constructByRecord(record)

                newObject.setGrid(World.getGrid(newObject.pos))
                val result = newObject.getGridSafety().sendAndWaitAck(GridMessage.Spawn(newObject))
                if (!result) {
                    player.systemSay("Failed to spawn object")
                }
            }
        }
    }
}