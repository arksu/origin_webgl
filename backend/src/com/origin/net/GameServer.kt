package com.origin.net;

import com.origin.AccountCache;
import com.origin.Database;
import com.origin.entity.Account;
import com.origin.entity.Character;
import com.origin.model.Player;
import com.origin.model.World;
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

	private static final AccountCache accountCache = new AccountCache();

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
		if (session.getAccount() == null)
		{
			switch (target)
			{
				case "login":
					return login(session, data);
				case "register":
					return registerNewAccount(session, data);
			}
		}
		else
		{
			switch (target)
			{
				case "getCharacters":
					return getCharacters(session.getAccount(), data);
				case "createCharacter":
					return createCharacter(session.getAccount(), data);
				case "selectCharacter":
					return selectCharacter(session, data);
				case "deleteCharacter":
					return deleteCharacter(session.getAccount(), data);
			}
		}
		_log.warn("unknown command: {}", target);
		return null;
	}

	/**
	 * получить список персонажей
	 */
	public Object getCharacters(Account account, Map<String, Object> data) throws GameException
	{
		return Database.em().findAll(Character.class, "SELECT * FROM characters WHERE accountId=? limit 5", account.getId());
	}

	/**
	 * создать нового персонажа
	 */
	public Object createCharacter(Account account, Map<String, Object> data)
	{
		String name = (String) data.get("name");

		Character character = new Character();
		character.setName(name);
		character.setAccountId(account.getId());
		character.persist();

		return Database.em().findAll(Character.class, "SELECT * FROM characters WHERE accountId=? limit 5", account.getId());
	}

	/**
	 * удалить персонажа
	 */
	public Object deleteCharacter(Account account, Map<String, Object> data)
	{
		Character character = new Character();
		character.setId(Math.toIntExact((Long) data.get("id")));
		Database.em().remove(character);

		return Database.em().findAll(Character.class, "SELECT * FROM characters WHERE accountId=? limit 5", account.getId());
	}

	/**
	 * выбрать игровой персонаж
	 */
	public Object selectCharacter(GameSession session, Map<String, Object> data)
	{
		Character character = Database.em().findById(Character.class, Math.toIntExact((Long) data.get("id")));

		if (character == null)
		{
			throw new GameException("no such player");
		}
		Player player = new Player(character, session);
		if (!World.instance.spawnPlayer(player))
		{
			throw new GameException("player could not be spawned");
		}

		return character;
	}

	/**
	 * регистрация нового аккаунта
	 */
	public Object registerNewAccount(GameSession session, Map<String, Object> data) throws InterruptedException, GameException
	{
		Account account = new Account();

		account.setLogin(((String) data.get("login")));
		account.setPassword(((String) data.get("password")));
		account.setEmail(((String) data.get("email")));

		try
		{
			account.persist();
			return loginUser(session, account);
		} catch (RuntimeException e)
		{
			if (e.getCause() instanceof SQLException && "23000".equals(((SQLException) e.getCause()).getSQLState()))
			{
				throw new GameException("username busy");
			}
			else
			{
				throw new GameException("register failed");
			}
		} catch (Throwable e)
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

		final Account account = Database.em().findOne(Account.class, "login", login);

		if (account != null)
		{
			if (SCryptUtil.check(account.getPassword(), password))
			{
				_log.debug("user auth: " + account.getLogin());

				return loginUser(session, account);
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

	private Object loginUser(GameSession session, Account account) throws InterruptedException
	{
		session.setAccount(account);

		if (!accountCache.addWithAuth(account))
		{
			throw new GameException("ssid collision, please try again");
		}
		LoginResponse response = new LoginResponse();
		response.ssid = account.getSsid();
		return response;
	}
}
