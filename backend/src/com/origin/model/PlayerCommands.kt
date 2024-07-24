package com.origin.model

import com.origin.model.item.ItemFactory
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
                val typeId: Int = params[1].toIntOrNull() ?: ItemFactory.getTypeByName(params[1].lowercase())

                if (typeId > 0) {
                    val newItem = ItemFactory.create(typeId)
                    if (!player.inventory.putItem(newItem)) {
                        // TODO new item drop to ground
                    }
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
                val pos = ObjectPosition(x, y, player.pos)

                World.getGrid(pos).generateObject(t, pos)
            }
        }
    }
}