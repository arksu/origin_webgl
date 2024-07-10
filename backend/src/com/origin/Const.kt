package com.origin

typealias ObjectID = Long

fun String?.toObjectID(): ObjectID {
    return this?.toLong() ?: throw RuntimeException("no string value for ObjectID")
}

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

const val OPEN_DISTANCE = 3

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