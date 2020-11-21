package com.origin.utils

object MapUtils {
    /**
     * размер одного тайла в игровых единицах длины
     */
    const val TILE_SIZE = 12

    /**
     * количество тайлов в стороне грида
     */
    const val GRID_SIZE = 100

    /**
     * количество гридов в супергриде
     */
    const val SUPERGRID_SIZE = 50

    /**
     * длина стороны грида в игровых единицах
     */
    const val GRID_FULL_SIZE = GRID_SIZE * TILE_SIZE

    /**
     * полная длина супергрида в игровых единицах (НЕ тайлах)
     */
    const val SUPERGRID_FULL_SIZE = GRID_FULL_SIZE * SUPERGRID_SIZE
    const val GRID_SQUARE = GRID_SIZE * GRID_SIZE

    /**
     * размер блоба для хранения массива тайлов
     * сколько байт отводим под 1 тайл
     */
    const val GRID_BLOB_SIZE = GRID_SQUARE * 3
}