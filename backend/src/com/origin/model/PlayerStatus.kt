package com.origin.model

class PlayerStatus(val player: Player) : HumanStatus(player) {
    init {
        stamina = player.character.stamina
    }

    override fun save() {
        super.save()
        player.character.stamina = stamina
    }
}