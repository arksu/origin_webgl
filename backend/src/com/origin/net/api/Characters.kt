package com.origin.net.api

import io.ktor.application.*
import io.ktor.routing.*

fun Route.getCharactersList() {
    get("/characters") {

    }
}

fun Route.createCharacter() {
    post("/characters") {

    }
}

fun Route.deleteCharacter() {
    delete("/characters/{id}") {
        val id = call.parameters["id"]
    }
}

