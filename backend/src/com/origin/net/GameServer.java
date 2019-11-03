package com.origin.net;

import com.origin.User;
import com.origin.UserCache;
import com.origin.net.model.LoginResponse;
import com.origin.net.model.GameSession;
import com.origin.utils.GameException;

import java.net.InetSocketAddress;
import java.util.Map;

public class GameServer extends WSServer
{
	private static UserCache userCache = new UserCache();

	public GameServer(InetSocketAddress address, int decoderCount)
	{
		super(address, decoderCount);
	}

	@Override
	protected Object process(GameSession session, String target, Map<String, Object> data) throws Exception
	{
		switch (target.toLowerCase())
		{
			case "login":
				return login(session, data);
		}

		return null;
	}

	public Object login(GameSession session, Map<String, Object> data) throws InterruptedException, GameException
	{
		String login = ((String) data.get("login"));

		User user = new User();

		if (login.equals("root"))
		{
			if (!userCache.addUserAuth(user))
			{
				throw new GameException("user cache error");
			}

			LoginResponse response = new LoginResponse();
			response.ssid = user.getSsid();
			return response;
		}

		return null;
	}
}
