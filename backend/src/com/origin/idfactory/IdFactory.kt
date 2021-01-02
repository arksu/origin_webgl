package com.origin.idfactory

import com.origin.entity.GlobalVariables
import com.origin.utils.ObjectID

/**
 * простейшая реализация фабрики ид
 * тупо инкрементим ид
 * но "кешируем", берем CAPACITY инкрементим их и запоминаем. при этом в базу говорим что последний ид уже
 * lastId + freeCount, т.е. в базе храним заведомо больший ид чем выдаем сейчас
 * как только выдадим все из диапазона - увеличим последний ид в базе на CAPACITY
 * таким образом обновляем базу не каждый раз при получении ид а лишь раз в CAPACITY раз
 */
object IdFactory {
    /**
     * сколько свободных ид брать за раз
     */
    private const val CAPACITY = 2

    private const val KEY = "nextFreeId"

    private var lastId: ObjectID = GlobalVariables.getLong(KEY, 0)

    private var freeCount = 1

    @Synchronized
    fun getNext(): ObjectID {
        freeCount--
        if (freeCount <= 0) {
            extend()
        }
        return ++lastId
    }

    private fun extend() {
        freeCount += CAPACITY
        GlobalVariables.saveLong(KEY, lastId + freeCount)
        println("IdFactory extend freeCount=$freeCount lastId=$lastId")
    }
}