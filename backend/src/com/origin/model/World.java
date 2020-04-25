package com.origin.model;

import com.origin.ServerConfig;

/**
 * весь игровой мир
 */
public class World
{
	private static final World instance = new World();

	private final Continent[] _continents;

	public static World getInstance()
	{
		return instance;
	}

	/**
	 * создать игровой мир
	 */
	public World()
	{
		// создаем континенты
		_continents = new Continent[ServerConfig.WORLD_CONTINENTS_SIZE];
		for (int i = 0; i < ServerConfig.WORLD_CONTINENTS_SIZE; i++)
		{
			_continents[i] = new Continent(i);
		}
	}

	public Continent getContinent(int index)
	{
		return _continents[index];
	}

	public boolean addPlayer(Player player)
	{
		// TODO
		return true;
	}
}
