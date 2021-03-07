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

    // мощеный камень
    const val STONE: Byte = 10
    // вспаханная земля
    const val PLOWED: Byte = 11

    // лес хвойный
    const val FOREST_PINE: Byte = 13
    // лес лиственный
    const val FOREST_LEAF: Byte = 15

    // заросли
    const val THICKET: Byte = 16

    // газон
    const val GRASS: Byte = 17
    // пустошь
    const val HEATH: Byte = 18
    // болота
    const val MOOR: Byte = 21
    // топь
    const val SWAMP: Byte = 23
    const val SWAMP2: Byte = 25
    const val SWAMP3: Byte = 27

    // глина
    const val CLAY: Byte = 29
    // вытоптанная земля
    const val DIRT: Byte = 30
    // песок
    const val SAND: Byte = 32

    // деревянный пол
    const val FLOOR_WOOD: Byte = 34
    // каменный пол
    const val FLOOR_STONE: Byte = 36
    // пол в шахте
    const val FLOOR_MINE: Byte = 38

    // пещеры
    const val CAVE: Byte = 42
    // горы
    const val MOUNTAIN: Byte = 46

}
