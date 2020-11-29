package com.origin.net.api

import com.origin.LoginResponse
import com.origin.UserLogin
import com.origin.UserSignup
import com.origin.entity.Account
import com.origin.scrypt.SCryptUtil
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.slf4j.LoggerFactory
import java.sql.SQLException

val logger = LoggerFactory.getLogger("Auth")

fun Route.login() {
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


fun Route.signup() {
    post("/signup") {
        val userSignup = call.receive<UserSignup>()
        Thread.sleep(1000)

        val account = Account()
        account.login = userSignup.login
        account.password = userSignup.password
        account.email = userSignup.email

        try {
            // TODO save
//                account.persist()
            // TODO auth user
            call.respond(LoginResponse("123"))
        } catch (e: RuntimeException) {
            logger.error("register failed RuntimeException ${e.message}", e)
            if (e.cause is SQLException && "23000" == (e.cause as SQLException?)!!.sqlState) {
                val vendorCode = (e.cause as SQLException?)!!.errorCode
                if (vendorCode == 1062) {
                    call.respond(LoginResponse(null, "this username is busy"))
                } else {
                    call.respond(LoginResponse(null, "register failed, vendor code $vendorCode"))
                }
            } else {
                call.respond(LoginResponse(null, "register failed ${e.message}"))
            }
        } catch (e: Throwable) {
            logger.error("register failed Throwable ${e.message}", e)
            call.respond(LoginResponse(null, "register failed"))
        }
    }
}
