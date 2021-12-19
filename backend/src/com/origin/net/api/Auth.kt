package com.origin.net.api

import com.origin.entity.Account
import com.origin.entity.Accounts
import com.origin.net.GameServer
import com.origin.utils.scrypt.SCryptUtil
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("Auth")

data class UserLogin(val login: String, val hash: String)
data class UserSignup(val login: String, val email: String?, val password: String)
data class LoginResponse(val ssid: String)

@ObsoleteCoroutinesApi
fun Route.auth() {
    post("/login") {
        val userLogin = call.receive<UserLogin>()

        val account = transaction {
            val acc =
                Account.find { Accounts.login eq userLogin.login }.forUpdate().firstOrNull() ?: throw UserNotFound()

            if (SCryptUtil.check(acc.password, userLogin.hash)) {
//                acc.lastLogged = Timestamp(Date().time)
                GameServer.accountCache.addWithAuth(acc)
                acc
            } else {
                throw WrongPassword()
            }
        }

        logger.debug("user auth successful ${account.login}")
        call.respond(LoginResponse(account.ssid!!))
    }

    post("/signup") {
        val userSignup = call.receive<UserSignup>()
        val email =  if (userSignup.email != null && userSignup.email.isEmpty()) {
             null
        } else {
            userSignup.email
        }

        val account = transaction {
            val accountInDatabase =
                Account.find {
                    (Accounts.login eq userSignup.login) or ((Accounts.email.neq(null) and (Accounts.email eq email)))
                }.firstOrNull()
            if (accountInDatabase != null) throw UserExists()

            val acc = Account.new {
                login = userSignup.login
                password = userSignup.password
                this.email = userSignup.email
            }
            GameServer.accountCache.addWithAuth(acc)
            acc
        }

        logger.debug("user register successful ${account.login}")
        call.respond(LoginResponse(account.ssid!!))
    }
}
