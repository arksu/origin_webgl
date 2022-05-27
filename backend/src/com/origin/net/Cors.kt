package com.origin.net

import io.ktor.http.*
import io.ktor.server.plugins.cors.*

fun CORSConfig.cors() {
    allowMethod(HttpMethod.Post)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Put)
    allowHeader(HttpHeaders.ContentType)
    allowHeader(HttpHeaders.Authorization)
    anyHost()
}
