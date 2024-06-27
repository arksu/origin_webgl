package com.origin.controller

import com.origin.ObjectID
import com.origin.error.BadRequestException
import com.origin.jooq.tables.references.CHARACTER
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jooq.DSLContext
import org.jooq.Record1

data class CharacterResponseDTO(val id: ObjectID, val name: String)

data class CharacterSelectResponseDTO(val token: String)

data class CharacterCreateRequestDTO(val name: String) {
    fun validate(): Boolean {
        val regex = Regex("([a-z]|[A-Z])([a-z]|[A-Z]|[0-9]|[_-])*")
        return regex.matches(name)
    }
}

fun Route.characters(dsl: DSLContext) {
    route("/character") {

        /**
         * get all player's characters
         */
        get {
            withAccount { account ->
                val list = dsl.selectFrom(CHARACTER)
                    .where(CHARACTER.ACCOUNT_ID.eq(account.id))
                    .and(CHARACTER.DELETED.isFalse)
                    .limit(5)
                    .fetch()

                call.respond(mapOf("list" to list.map {
                    CharacterResponseDTO(it.id!!, it.name!!)
                }))
            }
        }

        /**
         * select character for enter world
         */
        post("/{id}/select") {
            println(call.parameters["id"])
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
            withAccount { account ->
                val request = call.receive<CharacterCreateRequestDTO>()
                if (!request.validate()) {
                    throw BadRequestException("Wrong name")
                }
                val count = dsl.selectCount()
                    .from(CHARACTER)
                    .where(CHARACTER.ACCOUNT_ID.eq(account.id))
                    .and(CHARACTER.DELETED.isFalse)
                    .fetchSingle()
                    .let(Record1<Int>::value1)

                if (count >= 5) {
                    throw BadRequestException("Characters limit exceed")
                }

                // TODO: new character spawn coordinates

                val saved = dsl.insertInto(CHARACTER)
                    .set(CHARACTER.ACCOUNT_ID, account.id)
                    .set(CHARACTER.NAME, request.name)
                    .returning()
                    .fetchSingle()

                call.respond(CharacterResponseDTO(saved.id!!, saved.name!!))
            }
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
