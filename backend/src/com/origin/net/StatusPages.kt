package com.origin.net

import com.origin.net.api.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*

fun StatusPages.Configuration.statusPages() {
    exception<UserNotFound> {
        call.respond(HttpStatusCode.Forbidden, "User not found")
    }
    exception<AuthorizationException> {
        call.respond(HttpStatusCode.Unauthorized, "Not authorized")
    }
    exception<WrongPassword> {
        call.respond(HttpStatusCode.Forbidden, "Wrong password")
    }
    exception<UserExists> {
        call.respond(HttpStatusCode.Forbidden, "User exists")
    }
    exception<BadRequest> { e ->
        call.respond(HttpStatusCode.BadRequest, e.msg)
    }
    exception<Throwable> { cause ->
        logger.error("error ${cause.javaClass.simpleName} - ${cause.message} ", cause)
        call.respond(HttpStatusCode.InternalServerError, cause.message ?: cause.javaClass.simpleName)
    }
}