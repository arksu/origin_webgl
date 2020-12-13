package com.origin.utils

import com.origin.DatabaseFactory
import com.origin.ServerConfig
import java.io.File
import javax.imageio.ImageIO

object MapImporter {
    @JvmStatic
    fun main(args: Array<String>) {
        ServerConfig.load()
        DatabaseFactory.init()

        val img = ImageIO.read(File("map.png"))

        if (img.width != IMG_SIZE || img.height != IMG_SIZE) throw RuntimeException("wrong image size")

        // идем по гридам
        for (sx in 0 until SUPERGRID_SIZE) {
            for (sy in 0 until SUPERGRID_SIZE) {

                for (gx in 0 until GRID_SIZE) {
                    for (gy in 0 until GRID_SIZE) {
                        val tx = sx * GRID_SIZE + gx
                        val ty = sy * GRID_SIZE + gy

                        val c = img.getRGB(tx, ty)

//                        val grid = Grid.new {}
                    }
                }

            }
        }
    }
}