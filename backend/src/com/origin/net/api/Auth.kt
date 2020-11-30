package com.origin.net.api

import com.origin.LoginResponse
import com.origin.UserLogin
import com.origin.UserSignup
import com.origin.entity.Account
import com.origin.entity.Accounts
import com.origin.scrypt.SCryptUtil
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.SQLException

val logger: Logger = LoggerFactory.getLogger("Auth")

fun Route.login() {
    post("/login") {
        val userLogin = call.receive<UserLogin>()

        val account = transaction {
            addLogger(StdOutSqlLogger)
            val acc = Account.find { Accounts.login eq userLogin.login }.forUpdate().firstOrNull()
                ?: throw UserNotFound()

            if (SCryptUtil.check(acc.password, userLogin.hash)) {
                acc.generateSessionId()
            }

            acc
        }

        if (account == null) {
            call.respond(LoginResponse(null, "account not found"))
        } else {
            try {
                if (SCryptUtil.check(account.password, userLogin.hash)) {
                    logger.debug("user auth successful ${account.login}")
                    // TODO auth , ssid
//                        if (!GameServer.accountCache.addWithAuth(account)) {
//                            throw GameException("ssid collision, please try again")
//                        }
                    call.respond(LoginResponse(account.ssid))
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

        var account: Account? = null

        transaction {
            account = Account.new {
                login = userSignup.login
                password = userSignup.password
            }
        }


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
