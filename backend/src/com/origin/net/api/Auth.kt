package com.origin.net.api


/*
private fun Routing.login() {
    post("/login") {
        val userLogin = call.receive<UserLogin>()
//            val account = Database.em().findOne(Account::class.java, "login", userLogin.login)
        val account: Account? = null

        if (account == null) {
            call.respond(LoginResponse(null, "account not found"))
        } else {
            try {
                if (SCryptUtil.check(account.password, userLogin.hash)) {
                    Launcher._log.debug("user auth successful ${account.login}")
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

 */

/*
private fun Routing.signup() {
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
            Launcher._log.error("register failed RuntimeException ${e.message}", e)
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
            Launcher._log.error("register failed Throwable ${e.message}", e)
            call.respond(LoginResponse(null, "register failed"))
        }
    }
}

 */