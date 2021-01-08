package com.origin

/**
 * рассылка и получение эвентов в кластере
 * шина не гарантирует 100% доставки сообщений всем членам кластера
 * (при сбоях сети и рестартах серверов сообщения не будут получены)
 * служит для обработки служебных (не критичных) событий в кластере
 */
object EventBus {

    /**
     * инициализировать EventBus на старте сервера
     */
    fun init() {

    }

    /**
     * послать сообщение на шину событий в кластер
     */
    fun publishMessage(channel: String, message: String) {

    }

    /**
     * обработка сообщений с шины эвентов
     */
    private fun processMessage(channel: String, message: String) {

    }

    /**
     * подписаться на шину событий в кластере
     */
    private fun subscribe() {

    }
}