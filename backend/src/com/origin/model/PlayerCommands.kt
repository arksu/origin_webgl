package com.origin.model

import com.origin.model.item.ItemFactory
import com.origin.model.`object`.ObjectsFactory
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
                val clazz = ItemFactory.getClassByName(params[1].lowercase())

                if (clazz != null) {
                    val newItem = ItemFactory.create(clazz)
                    player.inventory.spawnItem(newItem)
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
                // param 1 - type
                val clazz = ObjectsFactory.getClassByName(params[1])
                if (clazz != null) {
                    val pos = ObjectPosition(x, y, player.pos)
                    World.getGrid(pos).generateObject(clazz, pos)
                }
            }
        }
    }
}