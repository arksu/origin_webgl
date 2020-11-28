package com.origin.net

import io.ktor.features.*
import io.ktor.http.*

fun CORS.Configuration.cors() {
    method(HttpMethod.Post)
    header(HttpHeaders.ContentType)
    anyHost()
}