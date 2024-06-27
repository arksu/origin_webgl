package com.origin

import com.origin.jooq.tables.records.AccountRecord
import com.origin.util.LockByName
import java.util.concurrent.ConcurrentHashMap

/**
 * локальный кэш залогиненных юзеров
 * 1. у нас ровно один гейм севрер сервер. тогда храним тут мапу соответствия ssid->account entity
 * в кэше всегда актуальная сохраненная сущность. гарантируем что один и тот же юзер может авторизоваться только 1 раз
 * 2. у нас кластер и гейм серверов несколько. тогда хранить сущность в кэше нельзя, т.к. авторизоваться
 * аккаунт может на другом сервере и у нас будут не актуальные данные. в этом случае надо при каждом запросе грузить
 * аккаунт из базы (сверять ssid из запроса с ssid из базы), а локально вообще ничего не хранить. смысла в кэше в этом случае нет.
 */
class AccountCache {
    /**
     * храним объекты юзеров в памяти
     * ssid > Account
     */
    private val accountBySsid = ConcurrentHashMap<String, AccountRecord>()

    /**
     * храним сессии по ид юзера
     */
    private val accountSsidById = HashMap<Long, String>()

    /**
     * блокировки по ид юзера
     */
    private val namedLocks = LockByName<Long>()

    /**
     * добавить юзера в кэш с одновременной его авторизацией
     *
     * @return true если успешно добавили и в кэше НЕ было такого ssid, false в случае если коллизия ssid
     */
    fun add(account: AccountRecord) {
        val accountId = account.id ?: throw RuntimeException("no account id")
        // РЕАЛИЗАЦИЯ варианта 1 (описание выше)
        // заблокируем по ид аккаунта, дабы нельзя было одному акканту одновременно делать НЕ атомарные операции ниже
        val lock = namedLocks.getLock(accountId)
        try {
            lock.lock()
            // ищем старую сессию для этого аккаунта
            val oldSsid = accountSsidById[account.id]
            // если сессия найдена
            if (oldSsid != null) {
                // удалим старую сессию
                accountBySsid.remove(oldSsid)
            }
            // запомним новую сессию
            accountBySsid[account.ssid!!] = account
            accountSsidById[accountId] = account.ssid!!
        } finally {
            lock.unlock()
        }
    }

    fun get(ssid: String?): AccountRecord? {
        // РЕАЛИЗАЦИЯ варианта 1 (описание выше)
        return if (ssid == null) null else accountBySsid[ssid]
    }
}
