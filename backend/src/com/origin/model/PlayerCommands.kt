package com.origin.model

import com.origin.model.inventory.ItemType
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
                val typeId: Int = params[1].toIntOrNull() ?: ItemType.fromName(params[1].lowercase()).id

                val newItem = ObjectsFactory.createInventoryItem(typeId)
                if (!player.inventory.putItem(newItem)) {
                    // TODO new item drop to ground
                }
            }

            else -> {
//                player.session.send(CreatureSay(0, text, SYSTEM))("Unknown command: $cmd")
            }
        }
    }
}