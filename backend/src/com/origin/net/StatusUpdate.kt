package com.origin.net

import com.google.gson.annotations.SerializedName
import com.origin.ObjectID
import com.origin.model.Human

const val CUR_SHP = 0
const val CUR_HHP = 1
const val MAX_HP = 2
const val CUR_STAMINA = 3
const val MAX_STAMINA = 4
const val CUR_ENERGY = 5
const val MAX_ENERGY = 6

class StatusUpdate(obj: Human) : ServerMessage(ServerPacket.STATUS_UPDATE.n) {
    val id: ObjectID = obj.id

    @SerializedName("l")
    private val list = ArrayList<Attr>(7)

    class Attr(
        // type
        private val t: Int,
        // value
        private val v: Int
    )

    fun addAttribute(type: Int, value: Int) {
        list.add(Attr(type, value))
    }


}
