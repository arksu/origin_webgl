package com.origin.model;

import com.origin.entity.Grid;

import java.util.List;

/**
 * слой (уровень) земли
 */
public class LandLayer
{
	private final Instance _instance;

	/**
	 * уровень земли
	 */
	private final int _level;

	/**
	 * гриды
	 */
	private List<Grid> _grids;

	public LandLayer(Instance instance, int level)
	{
		_instance = instance;
		_level = level;
	}

	public List<Grid> getGrids()
	{
		return _grids;
	}

	public boolean spawnPlayer(Player player)
	{
		// находим гриды которые нужны для спавна игрока

		return true;
	}
}
