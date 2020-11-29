package com.origin.net

import com.origin.LoginResponse
import com.origin.UserLogin
import com.origin.entity.Account
import com.origin.net.api.logger
import com.origin.scrypt.SCryptUtil
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Routing.api() {
    post("/login") {
        val userLogin = call.receive<UserLogin>()
//            val account = Database.em().findOne(Account::class.java, "login", userLogin.login)
        val account: Account = Account()
        account.login = "ark"
        account.password = "123"

        if (account == null) {
            call.respond(LoginResponse(null, "account not found"))
        } else {
            try {
                if (SCryptUtil.check(account.password, userLogin.hash)) {
                    logger.debug("user auth successful ${account.login}")
                    Thread.sleep(1000)
                    // TODO auth , ssid
//                        if (!GameServer.accountCache.addWithAuth(account)) {
//                            throw GameException("ssid collision, please try again")
//                        }
                    call.respond(LoginResponse("123"))
                } else {
                    call.respond(LoginResponse(null, "wrong password"))
                }
            } catch (e: Exception) {
                call.respond(LoginResponse(null, "error ${e.message}"))
            }
        }
    }
}
