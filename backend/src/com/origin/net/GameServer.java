package com.origin.net;

import com.origin.Database;
import com.origin.UserCache;
import com.origin.entity.Character;
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

	/**
	 * обработка всех websocket запросов
	 */
	@Override
	protected Object process(GameSession session, String target, Map<String, Object> data) throws Exception
	{
		// если к сессии еще не привязан юзер
		if (session.getUser() == null)
		{
			switch (target)
			{
				case "login":
					return login(session, data);
				case "register":
					return register(session, data);
			}
		}
		else
		{
			switch (target)
			{
				case "getCharacters":
					return getCharacters(session.getUser(), data);
				case "createCharacter":
					return createCharacter(session.getUser(), data);
				case "selectCharacter":
					return selectCharacter(session.getUser(), data);
				case "deleteCharacter":
					return deleteCharacter(session.getUser(), data);
			}
		}
		_log.warn("unknown command: {}", target);
		return null;
	}

	/**
	 * получить список персонажей
	 */
	public Object getCharacters(User user, Map<String, Object> data) throws GameException
	{
		return Database.em().findAll(Character.class, "SELECT * FROM characters WHERE userId=? limit 5", user.getId());
	}

	/**
	 * создать нового персонажа
	 */
	public Object createCharacter(User user, Map<String, Object> data)
	{
		String name = (String) data.get("name");

		Character character = new Character();
		character.setName(name);
		character.setUserId(user.getId());
		character.persist();

		return Database.em().findAll(Character.class, "SELECT * FROM characters WHERE userId=? limit 5", user.getId());
	}

	/**
	 * удалить персонажа
	 */
	public Object deleteCharacter(User user, Map<String, Object> data)
	{
		Character character = new Character();
		character.setId(Math.toIntExact((Long) data.get("id")));
		Database.em().remove(character);

		return Database.em().findAll(Character.class, "SELECT * FROM characters WHERE userId=? limit 5", user.getId());
	}

	/**
	 * выбрать игровой персонаж
	 */
	public Object selectCharacter(User user, Map<String, Object> data)
	{
		// TODO
		return null;
	}

	/**
	 * регистрация нового аккаунта
	 */
	public Object register(GameSession session, Map<String, Object> data) throws InterruptedException, GameException
	{
		User user = new User();

		user.setLogin(((String) data.get("login")));
		user.setPassword(((String) data.get("password")));
		user.setEmail(((String) data.get("email")));

		try
		{
			user.persist();
			return loginUser(session, user);
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
	}

	/**
	 * вход в систему
	 */
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

				return loginUser(session, user);
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

	private Object loginUser(GameSession session, User user) throws InterruptedException
	{
		session.setUser(user);

		if (!userCache.addUserAuth(user))
		{
			throw new GameException("user cache error");
		}
		LoginResponse response = new LoginResponse();
		response.ssid = user.getSsid();
		return response;
	}
}
