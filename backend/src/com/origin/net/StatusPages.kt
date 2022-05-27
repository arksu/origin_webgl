package com.origin.net

import com.origin.net.api.*
import io.ktor.http.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
fun StatusPagesConfig.statusPages() {
    exception<UserNotFound> { call, _ ->
        call.respond(HttpStatusCode.Forbidden, "User not found")
    }
    exception<AuthorizationException> { call, e ->
        call.respond(HttpStatusCode.Unauthorized, e.message ?: "Not authorized")
    }
    exception<WrongPassword> { call, _ ->
        call.respond(HttpStatusCode.Forbidden, "Wrong password")
    }
    exception<UserExists> { call, _ ->
        call.respond(HttpStatusCode.Forbidden, "User exists")
    }
    exception<BadRequest> { call, e ->
        call.respond(HttpStatusCode.BadRequest, e.message!!)
    }
    exception<Throwable> { call, e ->
        logger.error("error ${e.javaClass.simpleName} - ${e.message} ", e)
        call.respond(HttpStatusCode.InternalServerError, e.message ?: e.javaClass.simpleName)
    }
}
