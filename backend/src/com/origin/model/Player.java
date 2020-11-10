package com.origin.model;

import com.origin.entity.Character;
import com.origin.entity.Grid;
import com.origin.net.model.GameSession;

/**
 * инстанс персонажа игрока в игровом мире (игрок)
 */
public class Player extends GameObject
{
	/**
	 * персонаж игрока (сущность хранимая в БД)
	 */
	private final Character _character;

	private GameSession _gameSession;

	/**
	 * одежда (во что одет игрок)
	 */
	private Paperdoll _paperdoll;

	/**
	 * координаты кэшируем в объекте (потом периодически обновляем в сущности Character)
	 */
	private int _x;
	private int _y;
	private int _level;
	private int _instanceId;

	/**
	 * текущий активный грид игрока
	 */
	private Grid _grid;

	public Player(Character character, GameSession gameSession)
	{
		_character = character;
		_gameSession = gameSession;
		_x = character.getX();
		_y = character.getY();
		_level = character.getLevel();
		_instanceId = character.getInstanceId();
	}

	public Character getCharacter()
	{
		return _character;
	}

	@Override
	public int getX()
	{
		return _x;
	}

	@Override
	public int getY()
	{
		return _y;
	}

	@Override
	public int getLevel()
	{
		return _level;
	}

	@Override
	public int getInstanceId()
	{
		return _instanceId;
	}

	public Paperdoll getPaperdoll()
	{
		return _paperdoll;
	}

	public void setPaperdoll(Paperdoll paperdoll)
	{
		_paperdoll = paperdoll;
	}
}
