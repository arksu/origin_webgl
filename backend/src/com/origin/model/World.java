package com.origin.model;

/**
 * весь игровой мир
 */
public class World
{
	private static final World instance = new World();

	private Continent[] _continents;

	/**
	 * создать игровой мир
	 */
	public World()
	{

	}

	public static World getInstance()
	{
		return instance;
	}

	public Continent getContinent(int index)
	{
		return _continents[index];
	}

	/**
	 * добавить игрока в мир
	 * @param player
	 * @return получилось ли добавить (заспавнить) игрока в мир
	 */
	public boolean addPlayer(Player player)
	{
		// TODO
		return true;
	}
}
