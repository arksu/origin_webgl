package com.origin.controller

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory


data class UserLoginRequestDTO(val login: String, val hash: String)
data class UserSignupRequestDTO(val login: String, val email: String?, val password: String)
data class LoginResponseDTO(val ssid: String)

fun Route.auth() {
    val logger: Logger = LoggerFactory.getLogger("Auth")

    post("/login") {
        val userLogin = call.receive<UserLoginRequestDTO>()

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
        val userSignup = call.receive<UserSignupRequestDTO>()
        val email =  if (userSignup.email != null && userSignup.email.isEmpty()) {
             null
        } else {
            userSignup.email
        }

//        val account = transaction {
//            val accountInDatabase =
//                Account.find {
//                    (Accounts.login eq userSignup.login) or ((Accounts.email.neq(null) and (Accounts.email eq email)))
//                }.firstOrNull()
//            if (accountInDatabase != null) throw UserExists()
//
//            val acc = Account.new {
//                login = userSignup.login
//                password = userSignup.password
//                this.email = userSignup.email
//            }
//            GameServer.accountCache.addWithAuth(acc)
//            acc
//        }
//
//        logger.debug("user register successful ${account.login}")
//        call.respond(LoginResponse(account.ssid!!))
    }
}
