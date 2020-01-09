package com.origin.net.model;

import com.origin.entity.User;
import com.origin.net.WSServer;
import org.java_websocket.WebSocket;

public class GameSession
{
	private final WebSocket _connect;

	private final String _remoteAddr;

	private User _user;

	public GameSession(WebSocket connect, String remoteAddr)
	{
		_connect = connect;
		_remoteAddr = remoteAddr;
	}

	public String getRemoteAddr()
	{
		return _remoteAddr;
	}

	public void send(String channel, Object data)
	{
		if (_connect != null && _connect.isOpen())
		{
			WSResponse response = new WSResponse();
			response.id = 0;
			response.data = data;
			response.channel = channel;

			_connect.send(WSServer.gsonSerialize.toJson(response));
		}
	}

	public void sendPing(String data)
	{
		if (_connect != null && _connect.isOpen())
		{
			_connect.send(data);
		}
	}

	public void setUser(User user)
	{
		_user = user;
	}

	public User getUser()
	{
		return _user;
	}
}
