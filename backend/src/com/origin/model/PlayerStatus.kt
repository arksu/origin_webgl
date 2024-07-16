package com.origin.model

class PlayerStatus(val player: Player) : HumanStatus(player) {
    init {
        stamina = player.character.stamina
    }
}