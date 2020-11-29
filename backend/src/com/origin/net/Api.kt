package com.origin.net

import com.origin.net.api.login
import com.origin.net.api.signup
import io.ktor.routing.*

fun Route.api() {
    route("/api") {
        login()
        signup()
    }
}
