package com.origin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class UserCache
{
	private static final int TIMEOUT_LOCK = 1000;

	private final ConcurrentHashMap<String, User> _users = new ConcurrentHashMap();

	private final ReentrantLock _lock = new ReentrantLock();

	public void drop(String ssid) throws InterruptedException
	{
		if (_lock.tryLock(TIMEOUT_LOCK, TimeUnit.MILLISECONDS))
		{
			try
			{
				_users.remove(ssid);
			}
			finally
			{
				_lock.unlock();
			}
		}
	}

	public String drop(User user) throws InterruptedException
	{
		if (_lock.tryLock(TIMEOUT_LOCK, TimeUnit.MILLISECONDS))
		{
			try
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
			}
			finally
			{
				_lock.unlock();
			}
		}
		return null;
	}

	public boolean addUserAuth(User user) throws InterruptedException
	{
		if (_lock.tryLock(TIMEOUT_LOCK, TimeUnit.MILLISECONDS))
		{
			try
			{
				if (_users.containsValue(user)) return false;
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
}
