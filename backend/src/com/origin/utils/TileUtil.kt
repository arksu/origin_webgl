package com.origin.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import javax.imageio.ImageIO
import kotlin.math.roundToInt

object TileUtil {
    val logger: Logger = LoggerFactory.getLogger(TileUtil::class.java)

    const val PATH = "../frontend/assets/tiles/water_backup/"
    const val OUT_PATH = "../frontend/assets/tiles/remake/"

    @JvmStatic
    fun main(args: Array<String>) {
        val mask = ImageIO.read(File("tile_mask.png"))

        Files.find(
            Paths.get(PATH), Int.MAX_VALUE,
            { _: Path, fileAttr: BasicFileAttributes -> fileAttr.isRegularFile })
            .forEach { x: Path ->
                val f = x.fileName.toString()
                logger.debug(f)

                if (x.toString().endsWith(".png")) {
                    val img = ImageIO.read(x.toFile())
                    logger.debug("process $x")
                    val result = process(mask, img)

                    val f = File(OUT_PATH + f)
                    try {
                        ImageIO.write(result, "png", f)
                    } catch (e: Exception) {
                        println("error ${e.message}")
                    }
                }
            }
    }

    private fun process(mask: BufferedImage, img: BufferedImage): BufferedImage {
        val r = BufferedImage(mask.width, mask.height, BufferedImage.TYPE_INT_ARGB)

        val ow = img.width.toDouble()
        val oh = img.height.toDouble()
        logger.debug("img: $ow $oh")
        val mw = mask.width.toDouble()
        val mh = mask.height.toDouble()
        logger.debug("mask: $mw $mh")

        for (x in 0 until mask.width) for (y in 0 until mask.height) {
            val m = mask.getRGB(x, y) and 0x00ffffff
            val color = 0
            var fill: Int = 0xffffff or (0xff shl 24)

            var c: Int = 0
            if (m > 0) {
                val kx: Double = (ow / mw) * x
                val ky: Double = (oh / mh) * y
//                logger.debug("$kx $ky")
                val vx = kx.roundToInt()
                val vy = ky.roundToInt()
//                logger.debug("$vx $vy")
                c = img.getRGB(vx, vy)
            }

            r.setRGB(x, y, c)
        }


        return r
    }
}