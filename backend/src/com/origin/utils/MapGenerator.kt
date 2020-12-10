package com.origin.utils

import com.origin.DatabaseFactory
import com.origin.ServerConfig

object MapGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        ServerConfig.load()
        DatabaseFactory.init()

        println("map generator start...")
    }
}