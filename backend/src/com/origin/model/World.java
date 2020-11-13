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
		// создаем грид в котором находится игрок
		Instance instance = _instances.computeIfAbsent(
				player.getInstanceId(),
				i -> new Instance(player.getInstanceId()));

		// сам инстанс уже спавнит игрока
		return instance.spawnPlayer(player);
	}
}
