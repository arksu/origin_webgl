package com.origin.model

import com.origin.net.StatusUpdate

class PlayerStatus(val player: Player) : HumanStatus(player) {
    init {
        stamina = player.character.stamina
    }

}