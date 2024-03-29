package com.origin.utils

import com.origin.ServerConfig
import com.origin.database.DatabaseFactory
import com.origin.entity.EntityObjects
import com.origin.entity.Grids
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.imageio.ImageIO

object MapImporter {
    val logger: Logger = LoggerFactory.getLogger(MapImporter::class.java)

    fun run() {
        ServerConfig.load()
        DatabaseFactory.init()

        // читаем картинку с картой
        val img = ImageIO.read(File("map.png"))

        // проверим размер картинки с картой
        if (img.width != IMG_SIZE || img.height != IMG_SIZE) throw RuntimeException("wrong image size")

        // удалим из базы вообще все гриды
        transaction {
            // очищаем таблицу гридов
            Grids.deleteAll()
            // и объектов. т.к. объекты будут генерироваться при первичной прогрузке грида
            // то есть практически полноценный ВАЙП сервера
            // инвентари только останутся. что может привести к багам (типа в дереве старый инвентарь какого то ящика)
            EntityObjects.deleteAll()
        }

        // отступ насколько сдвигаем импортируемую карту в базе
        // добавляем к координатам грида
        val gridOffsetX = 0
        val gridOffsetY = 0
        // континент в который импортируем карту
        val region = 0

        // идем по гридам
        for (sx in 0 until SUPERGRID_SIZE) {
            logger.info("process grid x $sx")
            for (sy in 0 until SUPERGRID_SIZE) {

                val ba = ByteArray(GRID_BLOB_SIZE)

                // идем по тайлам внутри грида
                for (gx in 0 until GRID_SIZE) {
                    for (gy in 0 until GRID_SIZE) {
                        val tx = sx * GRID_SIZE + gx
                        val ty = sy * GRID_SIZE + gy

                        val idx = gy * GRID_SIZE + gx
                        // читаем цвет карты
                        var c: Int = img.getRGB(tx, ty)

                        // уберем альфа канал
                        c = c and 0xffffff

                        // берем тип тайла из цвета
                        val tileType: Byte = when (c) {
                            TileColors.MEADOW_LOW -> Tile.GRASS
                            TileColors.MEADOW_HIGH -> Tile.HEATH
                            TileColors.FOREST_PINE -> Tile.FOREST_PINE
                            TileColors.FOREST_LEAF -> Tile.FOREST_LEAF
                            TileColors.TUNDRA -> Tile.HEATH // TODO
                            TileColors.PRAIRIE -> Tile.HEATH // TODO
                            TileColors.CLAY -> Tile.CLAY
                            TileColors.SAND -> Tile.SAND
                            TileColors.SWAMP -> Tile.SWAMP
                            TileColors.WATER -> Tile.WATER
                            TileColors.WATER_DEEP -> Tile.WATER_DEEP
                            else ->
                                throw RuntimeException("unknown tile 0x${c.toString(16)} at grid $gx $gy pixel $tx $ty")
                        }
                        ba[idx] = tileType
                    }
                }

                // пихаем в базу данные грида
                transaction {
                    Grids.insert {
                        it[x] = gridOffsetX + sx
                        it[y] = gridOffsetY + sy
                        it[level] = 0
                        it[Grids.region] = region
                        it[lastTick] = 0
                        it[tilesBlob] = ExposedBlob(ba)
                    }
                }
            }
        }

        logger.info("map import done")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        run()
    }
}
