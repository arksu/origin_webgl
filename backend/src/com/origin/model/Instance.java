package com.origin.model;

import java.util.concurrent.ConcurrentHashMap;

/**
 * игровой континент (материк)
 * в игре может быть несколько больших континентов одновременно
 */
public class Instance
{
	private final int _id;

	private final ConcurrentHashMap<Integer, LandLayer> _layers = new ConcurrentHashMap<>();

	public Instance(int id)
	{
		_id = id;
	}

	public boolean spawnPlayer(Player player)
	{
		LandLayer layer = _layers.computeIfAbsent(
				player.getInstanceId(),
				i -> new LandLayer(this, player.getLevel()));

		// сам уровень земли уже спавнит игрока
		return layer.spawnPlayer(player);

	}

}
