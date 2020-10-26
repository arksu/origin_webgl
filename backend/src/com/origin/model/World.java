package com.origin.model;

import com.origin.Database;
import com.origin.entity.Continent;
import com.origin.entity.Supergrid;

import java.util.List;

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
		// грузим все супергриды
		List<Supergrid> all = Database.em().findAll(Supergrid.class, "SELECT * FROM supergrids WHERE 1");

	}

	public static World getInstance()
	{
		return instance;
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
