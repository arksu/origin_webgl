package com.origin.controller

import com.origin.GameServer
import com.origin.error.UserAlreadyExists
import com.origin.jooq.tables.records.AccountRecord
import com.origin.jooq.tables.references.ACCOUNT
import com.origin.util.generateString
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jooq.DSLContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory


data class UserLoginRequestDTO(val login: String, val hash: String)
data class UserSignupRequestDTO(val login: String, val email: String?, val password: String)
data class LoginResponseDTO(val ssid: String)

fun Route.auth(dsl: DSLContext) {
    val logger: Logger = LoggerFactory.getLogger("Auth")

    post("/login") {
        val request = call.receive<UserLoginRequestDTO>()


//        val account = transaction {
//            val acc =
//                Account.find { Accounts.login eq userLogin.login }.forUpdate().firstOrNull() ?: throw UserNotFound()
//
//            if (SCryptUtil.check(acc.password, userLogin.hash)) {
////                acc.lastLogged = Timestamp(Date().time)
//                GameServer.accountCache.addWithAuth(acc)
//                acc
//            } else {
//                throw WrongPassword()
//            }
//        }

//        logger.debug("user auth successful ${account.login}")
//        call.respond(LoginResponse(account.ssid!!))
    }

    post("/signup") {
        val request = call.receive<UserSignupRequestDTO>()

        val email = if (request.email.isNullOrBlank()) {
            null
        } else {
            request.email.trim().lowercase()
        }

        val account = dsl.transactionResult { trx ->
            val account = trx.dsl()
                .selectFrom(ACCOUNT)
                .where(
                    ACCOUNT.LOGIN.eq(request.login.lowercase())
                        .or(ACCOUNT.EMAIL.isNotNull.and(ACCOUNT.EMAIL.eq(email)))
                )
                .forUpdate()
                .fetchOne()
            if (account != null) throw UserAlreadyExists()

            val newAccount = AccountRecord()
            newAccount.login = request.login.lowercase()
            newAccount.email = email
            newAccount.password = request.password
            newAccount.ssid = generateString(32)

            val saved = trx.dsl().insertInto(ACCOUNT)
                .set(newAccount)
                .returning()
                .fetchSingle()

            logger.debug("user register successful ${saved.login}")
            GameServer.accountCache.addWithAuth(saved)
            saved
        }

        call.respond(LoginResponseDTO(account.ssid!!))
    }
}
