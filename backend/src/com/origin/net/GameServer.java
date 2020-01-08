package com.origin.net;

import com.origin.Database;
import com.origin.UserCache;
import com.origin.entity.User;
import com.origin.net.model.GameSession;
import com.origin.net.model.LoginResponse;
import com.origin.scrypt.SCryptUtil;
import com.origin.utils.GameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Map;

public class GameServer extends WSServer
{
	private static final Logger _log = LoggerFactory.getLogger(GameServer.class.getName());

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
			case "register":
				return register(data);
		}

		return null;
	}

	public Object register(Map<String, Object> data) throws InterruptedException, GameException
	{
		User user = new User();

		user.setLogin(((String) data.get("login")));
		user.setPassword(((String) data.get("password")));
		user.setEmail(((String) data.get("email")));

		try
		{
			Database.em().persist(user);
		}
		catch (RuntimeException e)
		{
			if (e.getCause() instanceof SQLException && "23000".equals(((SQLException) e.getCause()).getSQLState()))
			{
				throw new GameException("username busy");
			}
			else
			{
				throw new GameException("register failed");
			}
		}
		catch (Throwable e)
		{
			throw new GameException("register failed");
		}

		return "ok";
	}

	public Object login(GameSession session, Map<String, Object> data) throws InterruptedException, GameException
	{
		final String login = ((String) data.get("login"));
		final String password = ((String) data.get("password"));

		final User user = Database.em().findOne(User.class, "login", login);

		if (user != null)
		{
			if (SCryptUtil.check(user.getPassword(), password))
			{
				_log.debug("user auth: " + user.getLogin());
				if (!userCache.addUserAuth(user))
				{
					throw new GameException("user cache error");
				}
				LoginResponse response = new LoginResponse();
				response.ssid = user.getSsid();
				return response;

			}
			else
			{
				throw new GameException("wrong password");
			}
		}
		else
		{
			throw new GameException("user not found");
		}
	}
}
