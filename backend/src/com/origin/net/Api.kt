package com.origin.net

import com.origin.net.api.*
import io.ktor.routing.*
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
fun Route.api() {
    route("/api") {
        login()
        signup()
        getCharactersList()
        createCharacter()
        deleteCharacter()
    }
}
