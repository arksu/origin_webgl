package com.origin.net

import com.google.gson.Gson
import com.origin.net.model.GameSession
import com.origin.net.model.WSRequest
import com.origin.net.model.WSResponse
import com.origin.utils.GameException
import com.origin.utils.MapDeserializerDoubleAsIntFix.gsonDeserialize
import com.origin.utils.Utils
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * вебсокет сервер, реализация сети вебгл клиента
 */
abstract class WSServer(address: InetSocketAddress?, decoderCount: Int) : WebSocketServer(address, decoderCount, null) {
    /**
     * ping таски
     */
    private val executor: ScheduledExecutorService

    init {
        isReuseAddr = true
        executor = Executors.newScheduledThreadPool(decoderCount)
    }

    private var isRunning = false

    /**
     * список активных вебсокет сессий
     */
    val sessions: MutableMap<WebSocket, GameSession> = ConcurrentHashMap()

    companion object {
        private val _log = LoggerFactory.getLogger(WSServer::class.java.name)

        /**
         * время между получением пинга и отправкой ответного пинга клиенту
         */
        private const val PING_TIME = 15

        @JvmField
        val gsonSerialize = Gson()


        private fun getRemoteAddr(conn: WebSocket?): String {
            return if (conn != null && conn.remoteSocketAddress != null && conn.remoteSocketAddress.address != null) {
                conn.remoteSocketAddress.address.hostAddress
            } else {
                "null"
            }
        }
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        // если сервер запущен за nginx смотрим реальный ип в заголовке X-Real-IP который пробросили в nginx
        var remoteAddr = handshake.getFieldValue("X-Real-IP")
        _log.debug("ws open " + getRemoteAddr(conn) + " xRealIp=" + remoteAddr)
        if (Utils.isEmpty(remoteAddr)) {
            remoteAddr = getRemoteAddr(conn)
        }
        val session = GameSession(conn, remoteAddr)
        sessions[conn] = session

        // запустим таск на отправку пинга клиенту
        executor.schedule(PingTask(session), PING_TIME.toLong(), TimeUnit.SECONDS)
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        _log.debug("ws close " + getRemoteAddr(conn))
        val session = sessions.remove(conn)
        session?.let { onSessionClosed(it) }
    }

    protected fun onSessionClosed(session: GameSession?) {}

    override fun onMessage(conn: WebSocket, message: String) {
        _log.debug("ws msg: " + getRemoteAddr(conn) + " " + message)
        val session = sessions[conn]
        if (session == null) {
            _log.error("no game session " + conn.remoteSocketAddress)
            throw IllegalStateException("no game session")
        }
        if ("ping" == message) {
            executor.schedule(PingTask(session), PING_TIME.toLong(), TimeUnit.SECONDS)
        } else {
            // десериализуем сообщение
            val request = gsonDeserialize!!.fromJson(message, WSRequest::class.java)
            val response = WSResponse()
            response.id = request.id
            try {
                // обработаем запрос к серверу, получим ответ
                response.data = process(session, request.target, request.data)
            } catch (e: GameException) {
                _log.error("GameException " + e.message, e)
                response.success = 0
                response.errorText = e.message
            } catch (e: Exception) {
                _log.error("Exception " + e.message, e)
                response.success = 0
                response.errorText = e.javaClass.simpleName + " " + e.message
            }
            conn.send(gsonSerialize.toJson(response))
        }
    }

    override fun onError(conn: WebSocket, ex: Exception) {
        _log.error("ws error: " + getRemoteAddr(conn) + " " + ex.message, ex)
    }

    override fun onStart() {
        _log.debug("ws net started")
        isRunning = true
    }

    @Throws(Exception::class)
    protected abstract fun process(session: GameSession, target: String?, data: Map<String, Any>): Any?

    /**
     * таск отправки пинга клиенту
     */
    private class PingTask(private val _session: GameSession?) : Runnable {
        override fun run() {
            _log.debug("send ping")
            _session!!.sendPing("ping")
        }
    }

}