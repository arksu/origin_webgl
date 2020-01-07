package com.origin.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.origin.net.model.GameSession;
import com.origin.net.model.WSRequest;
import com.origin.net.model.WSResponse;
import com.origin.utils.GameException;
import com.origin.utils.MapDeserializerDoubleAsIntFix;
import com.origin.utils.Utils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * вебсокет сервер, реализация сети вебгл клиента
 */
public abstract class WSServer extends WebSocketServer
{
	private static final Logger _log = LoggerFactory.getLogger(WSServer.class.getName());

	/**
	 * время жизни кэша запросов сессии
	 */
	private static final int SESSION_CACHE_TIMEOUT = 5 * 60 * 1000;

	/**
	 * время между получением пинга и отправкой ответного пинга клиенту
	 */
	private static final int PING_TIME = 15;

	public static Gson gsonSerialize = new Gson();
	private static Gson gsonDeserialize;

	/**
	 * ping таски
	 */
	private static volatile ScheduledExecutorService _executor;

	static
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(new TypeToken<Map<String, Object>>() {}.getType(), new MapDeserializerDoubleAsIntFix());
		gsonDeserialize = gsonBuilder.create();
	}

	private boolean _isRunning = false;

	/**
	 * список активных вебсокет сессий
	 */
	private Map<WebSocket, GameSession> _sessions = new ConcurrentHashMap<>();

	/**
	 * кэш ssid (храним ssid и время последнего обращения с этим ssid)
	 * в фоне поток очищает старые ssid из этой мапы
	 */
	private Map<String, Long> _ssidTimeCache = new ConcurrentHashMap<>();

	/**
	 * кэш ответов сервера
	 */
	private Map<String, LinkedList<WSResponse>> _responseCache = new ConcurrentHashMap<>();

	public WSServer(InetSocketAddress address, int decoderCount)
	{
		super(address, decoderCount, null);
		setReuseAddr(true);

		_executor = Executors.newScheduledThreadPool(decoderCount);
	}

	private static String getRemoteAddr(WebSocket conn)
	{
		if (conn != null && conn.getRemoteSocketAddress() != null && conn.getRemoteSocketAddress().getAddress() != null)
		{
			return conn.getRemoteSocketAddress().getAddress().getHostAddress();
		}
		else
		{
			return "null";
		}
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake)
	{
		// если сервер запущен за nginx смотрим реальный ип в заголовке X-Real-IP который пробросили в nginx
		String remoteAddr = handshake.getFieldValue("X-Real-IP");
		_log.debug("ws open " + getRemoteAddr(conn) + " xRealIp=" + remoteAddr);
		if (Utils.isEmpty(remoteAddr))
		{
			remoteAddr = getRemoteAddr(conn);
		}

		GameSession session = new GameSession(conn, remoteAddr);
		_sessions.put(conn, session);

		// запустим таск на отправку пинга клиенту
		_executor.schedule(new PingTask(session), PING_TIME, TimeUnit.SECONDS);
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote)
	{
		_log.debug("ws close " + getRemoteAddr(conn));

		GameSession session = _sessions.remove(conn);
		if (session != null)
		{
			onSessionClosed(session);
		}
	}

	protected void onSessionClosed(GameSession session)
	{
	}

	@Override
	public void onMessage(WebSocket conn, String message)
	{
		_log.debug("ws msg: " + getRemoteAddr(conn) + " " + message);

		GameSession session = _sessions.get(conn);
		if (session == null)
		{
			_log.error("no game session " + conn.getRemoteSocketAddress());
		}

		if ("ping".equals(message))
		{
			_executor.schedule(new PingTask(session), PING_TIME, TimeUnit.SECONDS);
		}
		else
		{
			// десериализуем сообщение
			WSRequest request = gsonDeserialize.fromJson(message, WSRequest.class);

			WSResponse response = new WSResponse();
			response.id = request.id;

			try
			{
				// если есть данные получим ssid
				if (request.data != null)
				{
					String ssid = (String) request.data.get("ssid");
					if (!Utils.isEmpty(ssid))
					{
						// по ssid и id запроса узнаем был ли такой уже в кэше
						// если есть - то вернем его иначе обработаем запрос как обычно
						LinkedList<WSResponse> queue = _responseCache.get(ssid);
						if (queue != null)
						{
							// ищем по всей очереди
							for (WSResponse r : queue)
							{
								// совпадение по id запроса
								if (r.id == request.id)
								{
									_log.warn("ws resp from cache [" + request.id + "]");
									// получим данные ответа
									response.data = r.data;
									// обновим время жизни в кэше
									_ssidTimeCache.put(ssid, new Date().getTime());
									break;
								}
							}
						}
					}
				}
				// если данные выше не получили из кэша
				if (response.data == null)
				{
					// обработаем запрос к серверу, получим ответ
					response.data = process(session, request.target, request.data);

					// если есть данные в запросе
					if (request.data != null)
					{
						String ssid = (String) request.data.get("ssid");
						if (!Utils.isEmpty(ssid))
						{
							// сохраним время ssid
							_ssidTimeCache.put(ssid, new Date().getTime());
							// создадим очередь ответов в кэше
							LinkedList<WSResponse> queue = _responseCache.computeIfAbsent(ssid, k -> new LinkedList<>());
							// ограничим размер кэша ответов сервера
							while (queue.size() > 32)
							{
								queue.poll();
							}
							// сохраним ответ в кэше
							queue.add(response);
						}
					}
				}
			}
			catch (GameException e)
			{
				_log.error("GameException " + e.getMessage(), e);
				response.success = 0;
				response.errorText = e.getMessage();
			}
			catch (Exception e)
			{
				_log.error("Exception " + e.getMessage(), e);
				response.success = 0;
				response.errorText = e.getClass().getSimpleName() + " " + e.getMessage();
			}

			conn.send(gsonSerialize.toJson(response));
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex)
	{
		_log.error("ws error: " + getRemoteAddr(conn) + " " + ex.getMessage(), ex);
	}

	@Override
	public void onStart()
	{
		_log.debug("ws net started");

		// запускаем таск очистки кэша ответов сервера
		new CleanerThread().start();

		_isRunning = true;
	}

	protected abstract Object process(GameSession GameSession, String target, Map<String, Object> data) throws Exception;

	public Map<WebSocket, GameSession> getSessions()
	{
		return _sessions;
	}

	/**
	 * таск отправки пинга клиенту
	 */
	private static class PingTask implements Runnable
	{
		private final GameSession _session;

		public PingTask(GameSession session)
		{
			_session = session;
		}

		@Override
		public void run()
		{
			_session.sendPing("ping");
		}
	}

	/**
	 * таск очистки кэша ответов сервера
	 */
	class CleanerThread extends Thread
	{
		@Override
		public void run()
		{
			while (_isRunning)
			{
				cleanSessions();
				try
				{
					Thread.sleep(SESSION_CACHE_TIMEOUT / 2);
				}
				catch (InterruptedException e)
				{
					_log.error("InterruptedException", e);
				}
			}
		}

		private void cleanSessions()
		{
			long currentTime = new Date().getTime();
			for (String k : _ssidTimeCache.keySet())
			{
				if (currentTime > (_ssidTimeCache.get(k) + SESSION_CACHE_TIMEOUT))
				{
					_log.warn("delete ssid from cache: " + k);
					_ssidTimeCache.remove(k);
					_responseCache.remove(k);
				}
			}
		}
	}
}
