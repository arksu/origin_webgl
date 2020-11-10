package com.origin;

import com.origin.entity.Account;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * локальный кэш залогиненных юзеров
 */
public class AccountCache
{
	private static final int TIMEOUT_LOCK = 1000;

	/**
	 * храним объекты юзеров в памяти
	 */
	private final ConcurrentHashMap<String, Account> _accounts = new ConcurrentHashMap<>();

	/**
	 * блокировка на любые операции с кэшем
	 */
	private final ReentrantLock _lock = new ReentrantLock();

	public void drop(String ssid)
	{
		_accounts.remove(ssid);
	}

	public String drop(Account account)
	{
		for (Map.Entry<String, Account> entry : _accounts.entrySet())
		{
			if (entry.getValue().getId() == account.getId())
			{
				String ssid = entry.getKey();
				_accounts.remove(ssid);
				return ssid;
			}
		}
		return null;
	}

	/**
	 * добавить юзера в кэш с одновременной его авторизацией
	 */
	public boolean addWithAuth(Account account) throws InterruptedException
	{
		// операции добавления юзера в кэш позволим делать только по 1 одновременно
		// поэтому используем блокировку
		if (_lock.tryLock(TIMEOUT_LOCK, TimeUnit.MILLISECONDS))
		{
			try
			{
				if (_accounts.containsValue(account))
				{
					return false;
				}
				account.generateSessionId();
				_accounts.put(account.getSsid(), account);
				return true;
			}
			finally
			{
				_lock.unlock();
			}
		}
		return false;
	}

	public void addUser(Account account) throws InterruptedException
	{
		if (_lock.tryLock(TIMEOUT_LOCK, TimeUnit.MILLISECONDS))
		{
			// TODO
		}
	}
}