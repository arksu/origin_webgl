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
const val GRID_SQUARE = GRID_SIZE * GRID_SIZE

/**
 * размер блоба для хранения массива тайлов
 * сколько байт отводим под 1 тайл
 */
const val GRID_BLOB_SIZE = GRID_SQUARE * 3

// TILES

// луг (низкие травы)
val MEADOW_LOW = 0xd97cc0

// луг (высокие травы)
val MEADOW_HIGH = 0x8ebf8e

// лес лиственный
val FOREST_LEAF = 0x17d421

// лес хвойный
val FOREST_PINE = 0x316117

// глина
val CLAY = 0x70390f

// песок
val SAND = 0xe0e034

// степь
val PRAIRIE = 0xf0a01f

// болото
val SWAMP = 0x1c3819

// тундра
val TUNDRA = 0x3c7a6f

// мелководье
val WATER = 0x0055ff

// глубокая вода
val WATER_DEEP = 0x0000ff