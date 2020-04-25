package com.origin.model;

/**
 * базовый игровой объект в игровой механике
 * все игровые сущности наследуются от него
 */
public abstract class GameObject
{
	/**
	 * работа с координатами
	 */
	public abstract int getX();

	public abstract int getY();

	public abstract int getLevel();

	public abstract int getInstanceId();
}
