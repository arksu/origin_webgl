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

    const val PATH = "../frontend/assets/tiles/grass_orig/"
    const val OUT_PATH = "../frontend/assets/tiles/remake/"
    const val RED = ((0xff) shl 16) + (0xff shl 24)

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
                        logger.error("error ${e.message}")
                    }
                }
                return@forEach
            }
    }

    private fun process(mask: BufferedImage, img: BufferedImage): BufferedImage {
        val r = BufferedImage(mask.width, mask.height, BufferedImage.TYPE_INT_ARGB)

        val ow = img.width.toDouble()
        val oh = img.height.toDouble()
        logger.debug("img: ${img.width} ${img.height}")

        val mw = mask.width.toDouble()
        val mh = mask.height.toDouble()
        logger.debug("mask: ${mask.width} ${mask.height}")



        for (y in 0 until mask.height) {
            var correctionX = -1
            var oldMask = 0xff
            var prevA = -1
            for (x in 0 until mask.width) {
                val m = mask.getRGB(x, y) and 0x00ffffff

                val ky: Double = (oh / mh) * y
                val kx: Double = (ow / mw) * x

//                val kx: Double = (mw / ow) * x
//                val ky: Double = (mh / oh) * y

                val vx = kx.roundToInt()
                val vy = ky.roundToInt()

                var c = 0
                if (m > 0) {
                    c = img.getRGB(vx, vy)
                }
                val a = (img.getRGB(vx, vy) shr 24) and 0xff

                // построчно смотрим переход маски эти края обработаем по особому
                if (oldMask != 0xff && oldMask != m && a < 10) {
                    if (m > 1) {
                        var cc = calcColor(img, vx, vy, Pair(0, 0), Pair(1, 0), Pair(2, 0))
                        if (cc != -1) r.setRGB(x, y, cc)
                        cc = calcColor(img, vx, vy, Pair(0, 0), Pair(1, 0), Pair(2, 0))
                        if (cc != -1) r.setRGB(x + 1, y, cc)
                        correctionX = x + 1
                    } else {
                        var cc = calcColor(img, vx, vy, Pair(-1, 0), Pair(-2, 0), Pair(-3, 0))
                        if (cc != -1) r.setRGB(x - 1, y, cc)

                        if (prevA == 0) {
                            cc = calcColor(img, vx - 1, vy, Pair(-1, 0), Pair(-2, 0), Pair(-3, 0))
                            if (cc != -1) r.setRGB(x - 2, y, cc)
                        }
                    }
                } else {
                    if (x != correctionX && m > 0 && a > 10) {
                        when (Rnd.next(10)) {
                            0, 9 -> r.setRGB(x, y, c)
                            1 -> {
                                val cc = calcColor(img, vx, vy, Pair(0, 0), Pair(-1, 0), Pair(1, 0))
                                if (cc != -1) r.setRGB(x, y, cc)
                            }
                            2 -> {
                                val cc = calcColor(img, vx, vy, Pair(0, 0), Pair(0, -1), Pair(0, 1))
                                if (cc != -1) r.setRGB(x, y, cc)
                            }
                            3 -> {
                                val cc = calcColor(img, vx, vy, Pair(0, 0), Pair(1, 0))
                                if (cc != -1) r.setRGB(x, y, cc)
                            }
                            4 -> {
                                val cc = calcColor(img, vx, vy, Pair(0, 0), Pair(-1, 0))
                                if (cc != -1) r.setRGB(x, y, cc)
                            }
                            5 -> {
                                val cc = calcColor(img, vx, vy, Pair(0, 0), Pair(-1, 0), Pair(0, -1))
                                if (cc != -1) r.setRGB(x, y, cc)
                            }
                            6 -> {
                                val cc = calcColor(img, vx, vy, Pair(0, 0), Pair(1, 0), Pair(0, 1))
                                if (cc != -1) r.setRGB(x, y, cc)
                            }
                            7 -> {
                                val cc = calcColor(img, vx, vy, Pair(0, 0), Pair(0, 1))
                                if (cc != -1) r.setRGB(x, y, cc)
                            }
                            8 -> {
                                val cc = calcColor(img, vx, vy, Pair(0, 0), Pair(0, -1))
                                if (cc != -1) r.setRGB(x, y, cc)
                            }
                        }

                    }
                }
                oldMask = m
                prevA = a
            }
        }

        return r
    }

    private fun calcColor(src: BufferedImage, x: Int, y: Int, vararg offsets: Pair<Int, Int>): Int {
        val flag = true

        if (!flag) {
            return RED
        } else {
            var rsum = 0
            var gsum = 0
            var bsum = 0
            var cnt = 0

            for (pp in offsets) {
                val (ox, oy) = pp
                if (x + ox < 0 || x + ox >= src.width) continue
                if (y + oy < 0 || y + oy >= src.height) continue

                val c = src.getRGB(x + ox, y + oy)

                val a = (c shr 24) and 0xff
                val r = (c shr 16) and 0xff
                val g = (c shr 8) and 0xff
                val b = (c) and 0xff

                if (a > 200) {
                    rsum += r
                    gsum += g
                    bsum += b
                    cnt++
                }
            }
            return if (cnt > 0) {
                (bsum / cnt) + ((gsum / cnt) shl 8) + ((rsum / cnt) shl 16) + (0xff shl 24)
            } else {
                -1
            }
        }
    }
}