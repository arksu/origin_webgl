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
				// обработаем запрос к серверу, получим ответ
				response.data = process(session, request.target, request.data);
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
}
