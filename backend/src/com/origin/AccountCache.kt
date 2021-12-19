package com.origin

import com.origin.entity.Account
import com.origin.net.gameSessions
import com.origin.utils.LockByName
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

/**
 * локальный кэш залогиненных юзеров
 * 1. у нас ровно один гейм севрер сервер. тогда храним тут мапу соответствия ssid->account entity
 * в кэше всегда актуальная сохраненная сущность. гарантируем что один и тот же юзер может авторизоваться только 1 раз
 * 2. у нас кластер и гейм серверов несколько. тогда хранить сущность в кэше нельзя, т.к. авторизоваться
 * аккаунт может на другом сервере и у нас будут не актуальные данные. в этом случае надо при каждом запросе грузить
 * аккаунт из базы (сверять ssid из запроса с ssid из базы), а локально вообще ничего не хранить. смысла в кэше в этом случае нет.
 */
@ObsoleteCoroutinesApi
class AccountCache {
    /**
     * храним объекты юзеров в памяти
     * ssid > Account
     */
    private val accounts = ConcurrentHashMap<String, Account>()

    /**
     * храним сессии по ид юзера
     */
    private val accountsSsid = HashMap<Int, String>()

    /**
     * блокировки по ид юзера
     */
    private val namedLocks = LockByName<Int>()

    /**
     * добавить юзера в кэш с одновременной его авторизацией
     *
     * @return true если успешно добавили и в кэше НЕ было такого ssid, false в случае если коллизия ssid
     */
    fun addWithAuth(account: Account) {
        // РЕАЛИЗАЦИЯ варианта 1 (описание выше)
        // заблокируем по ид аккаунта, дабы нельзя было одному акканту одновременно делать НЕ атомарные операции ниже
        val lock = namedLocks.getLock(account.id.value)
        try {
            lock.lock()
            // ищем старую сессию для этого аккаунта
            val oldSsid = accountsSsid[account.id.value]
            // если сессия найдена
            if (oldSsid != null) {
                // удалим старую сессию
                accounts.remove(oldSsid)
                // TODO кикнуть все сессии такого же юзера надо искать по аккаунт ид а не по ssid
                // среди активных игровых сессий (коннектов) ищем с тем же ssid
//                gameSessions.forEach { s ->
//                    if (s.ssid == oldSsid) {
//                        runBlocking {
//                            s.kick()
//                        }
//                    }
//                }
            }
            do {
                // генерим аккаунту новый ssid
                account.generateSessionId()
                // обновляеем в кэше только если еще нет такого ssid
                val present = accounts.computeIfAbsent(account.ssid!!) { account }
            } while (present != account) // в случае коллизи ssid повторяем
            // запомним новую сессию
            accountsSsid[account.id.value] = account.ssid!!
        } finally {
            lock.unlock()
        }
    }

    fun get(ssid: String?): Account? {
        // РЕАЛИЗАЦИЯ варианта 1 (описание выше)
        return if (ssid == null) null else accounts[ssid]
    }
}
