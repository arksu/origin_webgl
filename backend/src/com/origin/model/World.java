package com.origin.model;

import java.util.concurrent.ConcurrentHashMap;

/**
 * весь игровой мир
 */
public class World
{
	public static final World instance = new World();

	private final ConcurrentHashMap<Integer, Instance> _instances = new ConcurrentHashMap<>();

	/**
	 * создать игровой мир
	 */
	public World()
	{

	}

	/**
	 * добавить игрока в мир
	 * @return получилось ли добавить (заспавнить) игрока в мир
	 */
	public boolean spawnPlayer(Player player)
	{
		Instance instance = _instances.computeIfAbsent(
				player.getInstanceId(),
				i -> new Instance(player.getInstanceId()));

		instance.getGrids();

		// TODO
		return true;
	}
}
