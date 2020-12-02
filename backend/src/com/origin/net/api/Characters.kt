package com.origin.net.api

import com.origin.entity.Account
import com.origin.entity.Character
import com.origin.entity.Characters
import com.origin.net.GameServer
import com.origin.net.GameServer.SSID_HEADER
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import org.jetbrains.exposed.sql.transactions.transaction

data class CharacterResponse(val id: Int, val name: String)

fun PipelineContext<Unit, ApplicationCall>.getAccountBySsid(): Account {
    return GameServer.accountCache.get(call.request.headers[SSID_HEADER]) ?: throw AuthorizationException()
}

fun Route.getCharactersList() {
    get("/characters") {
        val account = getAccountBySsid()
        val list = transaction {
            Character.find { Characters.account eq account.id.value }.map { c ->
                CharacterResponse(c.id.value, c.name)
            }
        }
        call.respond(mapOf("list" to list))
    }
}

fun Route.createCharacter() {
    post("/characters") {
        val account = getAccountBySsid()

    }
}

fun Route.deleteCharacter() {
    delete("/characters/{id}") {
        val account = getAccountBySsid()
        val id = call.parameters["id"]
    }
}

