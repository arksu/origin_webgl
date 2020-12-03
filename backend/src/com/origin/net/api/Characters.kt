package com.origin.net.api

import com.origin.entity.Account
import com.origin.entity.Character
import com.origin.entity.Characters
import com.origin.net.GameServer
import com.origin.net.GameServer.SSID_HEADER
import io.ktor.application.*
import io.ktor.request.*
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

data class CreateCharacter(val name: String)
data class CreateCharacterResponse(val name: String, val id: Int)

fun Route.createCharacter() {
    put("/characters") {
        val acc = getAccountBySsid()
        val data = call.receive<CreateCharacter>()

        val newChar = transaction {
            val c = Character.find { Characters.account eq acc.id }.count()
            if (c >= 5) {
                throw BadRequest("Characters limit exceed")
            }

            Character.new {
                account = acc
                name = data.name
                // TODO: spawn new character coordinates
                region = 0
                x = 0
                y = 0
                level = 0
            }
        }

        call.respond(CreateCharacterResponse(newChar.name, newChar.id.value))
    }
}

fun Route.deleteCharacter() {
    delete("/characters/{id}") {
        val account = getAccountBySsid()
        val id: Int = Integer.parseInt(call.parameters["id"])
        transaction {
            val char = Character.find { Characters.id eq id }.forUpdate().firstOrNull()
            if (char == null || char.account.id.value != account.id.value) {
                throw BadRequest("Wrong character")
            } else {
                char.delete()
            }
        }
        call.respond("ok")
    }
}

