package com.origin.controller

import com.origin.ObjectID
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

//fun PipelineContext<Unit, ApplicationCall>.getAccountBySsid(): Account {
//    return GameServer.accountCache.get(call.request.headers[SSID_HEADER]) ?: throw AuthorizationException()
//}

data class CharacterResponseDTO(val id: ObjectID, val name: String)

data class CharacterSelectResponseDTO(val token: String)

data class CharacterCreateRequestDTO(val name: String) {
    fun validate(): Boolean {
        val regex = Regex("([a-z]|[A-Z])([a-z]|[A-Z]|[0-9]|[_-])*")
        return regex.matches(name)
    }
}

fun Route.characters() {
    route("/character") {

        /**
         * get all player's characters
         */
        get() {
//            val account = getAccountBySsid()
//            val list = transaction {
//                Character.find { (Characters.account eq account.id) and (Characters.deleted eq false) }.limit(5)
//                    .map { c ->
//                        CharacterResponseDTO(c.id.value, c.name)
//                    }
//            }
//            call.respond(mapOf("list" to list))
        }

        /**
         * select character for enter world
         */
        post("{id}/select") {
//            val account = getAccountBySsid()
//            val selected = call.parameters["id"].toObjectID()
//            transaction {
//                account.selectedCharacter = selected
//                account.generateWsToken()
//            }
//            call.respond(CharacterSelectResponseDTO(account.wsToken!!))
        }

        /**
         * create new character
         */
        post {
//            val acc = getAccountBySsid()
//            val data = call.receive<CharacterCreateRequestDTO>()
//            if (!data.validate()) {
//                throw BadRequest("Wrong name")
//            }
//
//            val newChar = transaction {
//                val c = Character.find { (Characters.account eq acc.id) and (Characters.deleted eq false) }.count()
//                if (c >= 5) {
//                    throw BadRequest("Characters limit exceed")
//                }
//
//                // get new id from id factory
//                Character.new(IdFactory.getNext()) {
//                    account = acc
//                    name = data.name
//                    // TODO: new character spawn coordinates
//                    region = 0
//                    x = 0
//                    y = 0
//                    level = 0
//                    heading = 0
//
//                    SHP = 100.0
//                    HHP = 100.0
//                    stamina = 100.0
//                    energy = 6000.0
//                    hunger = 10.0
//                }
//            }
//
//            call.respond(CreateCharacterResponseDTO(newChar.name, newChar.id.value))
        }

        /**
         * delete character
         */
        delete("{id}") {
//            val account = getAccountBySsid()
//            val id: ObjectID = call.parameters["id"].toObjectID()
//            transaction {
//                val char = Character.find { Characters.id eq id }.forUpdate().firstOrNull()
//                if (char == null || char.account.id.value != account.id.value) {
//                    throw BadRequest("Wrong character")
//                } else {
//                    char.deleted = true
//                    char.flush()
//                }
//            }
//            call.respond(HttpStatusCode.NoContent)
        }
    }
}
