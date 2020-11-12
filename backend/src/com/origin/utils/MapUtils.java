package com.origin.utils;

public class MapUtils
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
}
