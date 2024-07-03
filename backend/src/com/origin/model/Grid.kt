package com.origin.model

import com.origin.config.DatabaseConfig
import com.origin.jooq.tables.records.GridRecord
import com.origin.jooq.tables.references.GRID

class Grid(
    private val record: GridRecord,
    val layer: LandLayer,
) {
    companion object {
        fun load(gx: Int, gy: Int, layer: LandLayer): Grid {
            val grid = DatabaseConfig.dsl
                .selectFrom(GRID)
                .where(GRID.X.eq(gx))
                .and(GRID.Y.eq(gy))
                .and(GRID.LEVEL.eq(layer.level))
                .and(GRID.REGION.eq(layer.region.id))
                .fetchOne() ?: throw RuntimeException("grid ($gx, $gy) level=${layer.level} region=${layer.region.id} is not found")
            return Grid(grid, layer)
        }
    }

    fun updateTiles() {
        val affected = DatabaseConfig.dsl
            .update(GRID)
            .set(GRID.TILES_BLOB, record.tilesBlob)
            .where(GRID.X.eq(record.x))
            .and(GRID.Y.eq(record.y))
            .and(GRID.REGION.eq(record.region))
            .and(GRID.LEVEL.eq(record.level))
            .execute()
        if (affected != 1) throw RuntimeException("failed update grid $record tiles")
    }
}