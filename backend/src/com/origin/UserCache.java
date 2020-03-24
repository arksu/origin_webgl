package com.origin;

import com.origin.entity.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * локальный кэш залогиненных юзеров
 */
public class UserCache
{
	private static final int TIMEOUT_LOCK = 1000;

	/**
	 * храним объекты юзеров в памяти
	 */
	private final ConcurrentHashMap<String, User> _users = new ConcurrentHashMap<>();

	/**
	 * блокировка на любые операции с кэшем
	 */
	private final ReentrantLock _lock = new ReentrantLock();

	public void drop(String ssid)
	{
		_users.remove(ssid);
	}

	public String drop(User user)
	{
		for (Map.Entry<String, User> entry : _users.entrySet())
		{
			if (entry.getValue().getId() == user.getId())
			{
				String ssid = entry.getKey();
				_users.remove(ssid);
				return ssid;
			}
		}
		return null;
	}

	/**
	 * добавить юзера в кэш с одновременной его авторизацией
	 */
	public boolean addUserAuth(User user) throws InterruptedException
	{
		// операции добавления юзера в кэш позволим делать только по 1 одновременно
		// поэтому используем блокировку
		if (_lock.tryLock(TIMEOUT_LOCK, TimeUnit.MILLISECONDS))
		{
			try
			{
				if (_users.containsValue(user))
				{
					return false;
				}
				user.generateSessionId();
				_users.put(user.getSsid(), user);
				return true;
			}
			finally
			{
				_lock.unlock();
			}
		}
		return false;
	}

	public void addUser(User user) throws InterruptedException
	{
		if (_lock.tryLock(TIMEOUT_LOCK, TimeUnit.MILLISECONDS))
		{
			// TODO
		}
	}
}