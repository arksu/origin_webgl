package com.origin.utils

import com.origin.DatabaseFactory
import com.origin.ServerConfig
import com.origin.entity.Grids
import com.origin.utils.TileColors.CLAY
import com.origin.utils.TileColors.FOREST_LEAF
import com.origin.utils.TileColors.FOREST_PINE
import com.origin.utils.TileColors.MEADOW_LOW
import com.origin.utils.TileColors.WATER
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import javax.imageio.ImageIO

object MapImporter {
    @JvmStatic
    fun main(args: Array<String>) {
        ServerConfig.load()
        DatabaseFactory.init()

        // читаем картинку с картой
        val img = ImageIO.read(File("map.png"))

        // проверим размер картинки с картой
        if (img.width != IMG_SIZE || img.height != IMG_SIZE) throw RuntimeException("wrong image size")

        // удалим из базы вообще все гриды
        transaction {
            Grids.deleteAll()
        }

        // отступ насколько сдвигаем импортируемую карту в базе
        // добавляем к координатам грида
        val gridOffsetX = 0
        val gridOffsetY = 0
        // контитент в который импортируем мапу
        val region = 0

        // идем по гридам
        for (sx in 0 until SUPERGRID_SIZE) {
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
                            MEADOW_LOW ->
                                3
                            FOREST_PINE ->
                                4
                            FOREST_LEAF ->
                                1
                            CLAY ->
                                5
                            WATER ->
                                2
                            else ->
                                throw RuntimeException("unknown tile $c")
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
                        it[tilesBlob] = ExposedBlob(ba);

                    }
                }
            }
        }

        println("map import done")
    }
}