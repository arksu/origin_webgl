package com.origin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.sql.Blob;

/**
 * игровой "чанк" (регион), базовый кусок карты
 */
@Entity
@Table(name = "grids", indexes = {
		@Index(name = "id_uniq", columnList = "supergrid, x, y, level", unique = true),
		@Index(name = "coord", columnList = "x, y, level")
})
public class Grid
{
	/**
	 * размер одного тайла в игровых единицах длины
	 */
	public static final int TILE_SIZE = 12;

	/**
	 * количество тайлов в стороне грида
	 */
	public static final int GRID_SIZE = 100;

	/**
	 * количество гридов в супергриде
	 */
	public static final int SUPERGRID_SIZE = 50;

	/**
	 * длина стороны грида в игровых единицах
	 */
	public static final int GRID_FULL_SIZE = GRID_SIZE * TILE_SIZE;

	/**
	 * полная длина супергрида в игровых единицах (НЕ тайлах)
	 */
	public static final int SUPERGRID_FULL_SIZE = GRID_FULL_SIZE * SUPERGRID_SIZE;

	public static final int GRID_SQUARE = GRID_SIZE * GRID_SIZE;

	/**
	 * размер блоба для хранения массива тайлов
	 * сколько байт отводим под 1 тайл
	 */
	public static final int GRID_BLOB_SIZE = GRID_SQUARE * 3;

	/**
	 * ид супергрида, по нему потом сделаем разбиение таблицы на партиции
	 * континент к которому оносится супергрид также зашит в ид супергрида
	 */
	@Column(name = "supergrid", columnDefinition = "INT(11) UNSIGNED NOT NULL", nullable = false)
	private int _supergrid;

	/**
	 * координаты грида внутри супергрида
	 */
	@Column(name = "x", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	private int _x;

	@Column(name = "y", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	private int _y;

	@Column(name = "level", columnDefinition = "INT(11) UNSIGNED NOT NULL")
	private int _level;

	/**
	 * сырые данные тайлов в виде массива байт, по 2 байта на 1 тайл
	 */
	@Column(name = "tiles", columnDefinition = "BLOB NOT NULL", nullable = false)
	private Blob _tilesBlob;

	public int getSupergrid()
	{
		return _supergrid;
	}

	public void setSupergrid(int supergrid)
	{
		_supergrid = supergrid;
	}

	public int getX()
	{
		return _x;
	}

	public void setX(int x)
	{
		_x = x;
	}

	public int getY()
	{
		return _y;
	}

	public void setY(int y)
	{
		_y = y;
	}

	public int getLevel()
	{
		return _level;
	}

	public void setLevel(int level)
	{
		_level = level;
	}

	public Blob getTilesBlob()
	{
		return _tilesBlob;
	}

	public void setTilesBlob(Blob tilesBlob)
	{
		_tilesBlob = tilesBlob;
	}
}
