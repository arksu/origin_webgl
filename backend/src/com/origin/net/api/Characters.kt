package com.origin.net.api

import com.origin.entity.Account
import com.origin.entity.Character
import com.origin.entity.Characters
import com.origin.idfactory.IdFactory
import com.origin.net.GameServer
import com.origin.net.GameServer.SSID_HEADER
import com.origin.utils.ObjectID
import com.origin.utils.toObjectID
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction


@ObsoleteCoroutinesApi
fun PipelineContext<Unit, ApplicationCall>.getAccountBySsid(): Account {
    return GameServer.accountCache.get(call.request.headers[SSID_HEADER]) ?: throw AuthorizationException()
}

data class CharacterResponse(val id: ObjectID, val name: String)

@ObsoleteCoroutinesApi
fun Route.getCharactersList() {
    get("/characters") {
        val account = getAccountBySsid()
        val list = transaction {
            Character.find { (Characters.account eq account.id) and (Characters.deleted eq false) }.limit(5).map { c ->
                CharacterResponse(c.id.value, c.name)
            }
        }
        call.respond(mapOf("list" to list))
    }
}

data class CreateCharacter(val name: String) {
    fun validate(): Boolean {
        val regex = Regex("([a-z]|[A-Z])([a-z]|[A-Z]|[0-9]|[_-])*")
        return regex.matches(name)
    }
}

data class CreateCharacterResponse(val name: String, val id: ObjectID)

@ObsoleteCoroutinesApi
fun Route.createCharacter() {
    put("/characters") {
        val acc = getAccountBySsid()
        val data = call.receive<CreateCharacter>()
        if (!data.validate()) {
            throw BadRequest("Wrong name")
        }

        val newChar = transaction {
            val c = Character.find { (Characters.account eq acc.id) and (Characters.deleted eq false)  }.count()
            if (c >= 5) {
                throw BadRequest("Characters limit exceed")
            }

            // get new id from id factory
            Character.new(IdFactory.getNext()) {
                account = acc
                name = data.name
                // TODO: new character spawn coordinates
                region = 0
                x = 0
                y = 0
                level = 0
                heading = 0

                SHP = 100.0
                HHP = 100.0
                stamina = 100.0
                energy = 6000.0
                hunger = 10.0

            }
        }

        call.respond(CreateCharacterResponse(newChar.name, newChar.id.value))
    }
}

@ObsoleteCoroutinesApi
fun Route.deleteCharacter() {
    delete("/characters/{id}") {
        val account = getAccountBySsid()
        val id: ObjectID = call.parameters["id"].toObjectID()
        transaction {
            val char = Character.find { Characters.id eq id }.forUpdate().firstOrNull()
            if (char == null || char.account.id.value != account.id.value) {
                throw BadRequest("Wrong character")
            } else {
                char.deleted = true
                char.flush()
            }
        }
        call.respondText("ok")
    }
}

