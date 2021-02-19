package com.origin.utils

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

/**
 * площадь грида в тайлах
 */
const val GRID_SQUARE = GRID_SIZE * GRID_SIZE

/**
 * размер блоба для хранения массива тайлов
 * сколько байт отводим под 1 тайл
 */
const val GRID_BLOB_SIZE = GRID_SQUARE * 3

// TILES
object TileColors {
    // пустшь, степь
    const val HEATH = 0x0

    // луг (низкие травы)
    const val MEADOW_LOW = 0xd97cc0

    // луг (высокие травы)
    const val MEADOW_HIGH = 0x8ebf8e

    // лес лиственный
    const val FOREST_LEAF = 0x17d421

    // лес хвойный
    const val FOREST_PINE = 0x316117

    // глина
    const val CLAY = 0x70390f

    // песок
    const val SAND = 0xe0e034

    // степь
    const val PRAIRIE = 0xf0a01f

    // болото
    const val SWAMP = 0x1c3819

    // тундра
    const val TUNDRA = 0x3c7a6f

    // мелководье
    const val WATER = 0x0055ff

    // глубокая вода
    const val WATER_DEEP = 0x0000ff
}

object Tile {
    const val WATER_DEEP: Byte = 1
    const val WATER: Byte = 3

    const val FOREST_LEAF: Byte = 10
    const val FOREST_PINE: Byte = 15

    const val MEADOW_LOW: Byte = 18

    const val CLAY: Byte = 20
    const val SAND: Byte = 22
}
