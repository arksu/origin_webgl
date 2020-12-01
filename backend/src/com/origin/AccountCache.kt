package com.origin

import com.origin.entity.Account
import java.util.concurrent.ConcurrentHashMap

/**
 * локальный кэш залогиненных юзеров
 */
class AccountCache {
    /**
     * храним объекты юзеров в памяти
     * ssid > Account
     */
    private val accounts = ConcurrentHashMap<String?, Account>()

    fun drop(ssid: String?) {
        // TODO
        accounts.remove(ssid)
    }

    /**
     * добавить юзера в кэш с одновременной его авторизацией
     *
     * @return true если успешно добавили и в кэше НЕ было такого ssid, false в случае если коллизия ssid
     */
    fun addWithAuth(account: Account) {
        do {
            // генерим аккаунту новый ssid
            account.generateSessionId()
            // обновляеем в кэше только если еще нет такого ssid
            val present = accounts.computeIfAbsent(account.ssid) { account }
            // если в кэше такой ssid был вернем ложь
        } while (present != account)
    }
}