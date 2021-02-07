package com.origin.net.model

import com.origin.model.Human
import com.origin.utils.ObjectID
import kotlinx.coroutines.ObsoleteCoroutinesApi

const val CUR_SHP = 0
const val CUR_HHP = 1
const val MAX_HP = 2
const val CUR_STAMINA = 3
const val MAX_STAMINA = 4
const val CUR_ENERGY = 5
const val MAX_ENERGY = 6

@ObsoleteCoroutinesApi
class StatusUpdate(obj: Human) : ServerMessage("su") {
    val id: ObjectID = obj.id
    private val list = ArrayList<Attr>()

    class Attr(private val i: Int, private val v: Int) {
    }

    fun addAttribute(id: Int, value: Int) {
        list.add(Attr(id, value))
    }
}