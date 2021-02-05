package com.origin.entity

import com.origin.database.timestamp
import org.jetbrains.exposed.dao.id.IntIdTable

object ChatHistory : IntIdTable("chatHistory") {

    val owner = long("owner")
    val channel = byte("channel")
    val text = varchar("text", 250)

    val created = timestamp("createTime", true)

    override fun createStatement(): List<String> {
        return listOf(super.createStatement()[0] + " ENGINE=MyISAM")
    }
}