package com.origin.controller

import com.origin.IdFactory
import com.origin.ObjectID
import com.origin.error.BadRequestException
import com.origin.jooq.tables.references.ACCOUNT
import com.origin.jooq.tables.references.CHARACTER
import com.origin.toObjectID
import com.origin.util.generateString
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jooq.DSLContext
import org.jooq.Record1
import kotlin.random.Random

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
                    CharacterResponseDTO(it.id, it.name)
                }))
            }
        }

        /**
         * select character for enter world
         */
        post("/{id}/select") {
            withAccount { account ->
                val selected = call.parameters["id"].toObjectID()
                dsl.selectFrom(CHARACTER)
                    .where(CHARACTER.ACCOUNT_ID.eq(account.id))
                    .and(CHARACTER.ID.eq(selected))
                    .and(CHARACTER.DELETED.isFalse)
                    .fetchOne() ?: throw BadRequestException("Character not found")

                val token = generateString(32)
                dsl.update(ACCOUNT)
                    .set(ACCOUNT.SELECTED_CHARACTER, selected)
                    .set(ACCOUNT.WS_TOKEN, token)
                    .where(ACCOUNT.ID.eq(account.id))
                    .execute()

                call.respond(CharacterSelectResponseDTO(token))
            }
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

                val saved = dsl.insertInto(CHARACTER)
                    .set(CHARACTER.ID, IdFactory.getNext())
                    .set(CHARACTER.ACCOUNT_ID, account.id)
                    .set(CHARACTER.NAME, request.name)
                    // TODO: new character spawn coordinates
                    .set(CHARACTER.X, Random.nextInt(1000))
                    .set(CHARACTER.Y, Random.nextInt(1000))
                    .set(CHARACTER.REGION, 0)
                    .set(CHARACTER.LEVEL, 0)
                    .set(CHARACTER.HEADING, 0)
                    .returning()
                    .fetchSingle()

                call.respond(CharacterResponseDTO(saved.id, saved.name))
            }
        }

        /**
         * delete character
         */
        delete("{id}") {
            withAccount { account ->
                val id = call.parameters["id"].toObjectID()
                val affectedRows = dsl.update(CHARACTER)
                    .set(CHARACTER.DELETED, 1)
                    .where(CHARACTER.ID.eq(id))
                    .and(CHARACTER.ACCOUNT_ID.eq(account.id))
                    .execute()

                if (affectedRows != 1) {
                    throw BadRequestException("Wrong character")
                }
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
