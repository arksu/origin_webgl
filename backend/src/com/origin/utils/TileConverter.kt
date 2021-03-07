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

const val PATH = "../frontend/assets/tiles/water/"

/**
 * ресайз и коррекция оригинальных тайлов
 */
object TileUtilResize {
    val logger: Logger = LoggerFactory.getLogger(TileUtilResize::class.java)

    const val OUT_PATH = "../frontend/assets/tiles/remake/"

    // красный цвет
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
                if (oldMask != 0xff && oldMask != m && a < 1) {
                    if (m > 1) {
                        var cc = calcColor(img, vx, vy, Pair(1, 0), Pair(2, 0), Pair(3, 0))
                        if (cc != -1) r.setRGB(x, y, cc)
                        cc = calcColor(img, vx + 1, vy, Pair(0, 0), Pair(1, 0), Pair(2, 0))
                        if (cc != -1) r.setRGB(x + 1, y, cc)
                        correctionX = x + 1
                    } else {
                        var cc = calcColor(img, vx, vy, Pair(-2, 0), Pair(-1, 0), Pair(-3, 0))
                        if (cc != -1) r.setRGB(x - 1, y, cc)

                        if (prevA == 0) {
                            cc = calcColor(img, vx - 1, vy, Pair(-2, 0), Pair(-1, 0), Pair(-3, 0))
                            if (cc != -1) r.setRGB(x - 2, y, cc)
                        }
                    }
                } else {
                    if (x != correctionX && m > 0 && a > 0) {
                        // вносим шум в скейл
                        // берем соседние тайлы и считаем средний цвет по разным алгоритмам
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
            var firstAlpha = -1

            var asum = 0
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

                if (firstAlpha < 0) {
                    if (a < 5) return -1
                    firstAlpha = a
                }

                if (a > 5) {
                    asum += a
                    rsum += r
                    gsum += g
                    bsum += b
                    cnt++
                }
            }
            return if (cnt > 0) {
//                (bsum / cnt) + ((gsum / cnt) shl 8) + ((rsum / cnt) shl 16) + ((0xff) shl 24)
                (bsum / cnt) + ((gsum / cnt) shl 8) + ((rsum / cnt) shl 16) + + ((asum / cnt) shl 24)
            } else {
                -1
            }
        }
    }
}

/**
 * переименование файлов в наш формат
 */
object TileUtilRename {
    val logger: Logger = LoggerFactory.getLogger(TileUtilRename::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        var baseCounters = 0
        val cornerCounters = HashMap<Int, Int>()
        val borderCounters = HashMap<Int, Int>()

        for (n in 0 until 300) {
            val fdata = File(PATH + "tile_${n}.data")
            val fpng = File(PATH + "tile_${n}.png")

            if (!fdata.exists()) continue

            if (!fpng.exists()) continue

            var tt = -1
            val lines = Files.readAllLines(fdata.toPath())


            for (li in 0 until lines.size) {
                val l = lines[li]
                if (l == "#Byte t") {
                    tt = lines[li + 1].toInt()
                }
                if (l == "#Byte id") {
                    val lid = lines[li + 1].toInt()
                    logger.debug("$fpng -> $lid [$tt]")

                    if (tt == 103) {
                        baseCounters++
                        val to = PATH + "base_${baseCounters}.png"
                        logger.warn("rename $fpng -> $to")
                        fpng.renameTo(File(to))
                    } else if (tt == 98) {
                        var c = borderCounters[lid]
                        if (c == null) c = 0
                        c++
                        borderCounters[lid] = c
                        val to = if (c > 1) PATH + "b${lid}_${c}.png" else PATH + "b${lid}.png"
                        logger.warn("rename $fpng -> $to")
                        fpng.renameTo(File(to))
                    } else if (tt == 99) {
                        var c = cornerCounters[lid]
                        if (c == null) c = 0
                        c++
                        cornerCounters[lid] = c

                        val fact = when (lid) {
                            1 -> 1
                            2 -> 8
                            3 -> 9
                            4 -> 4
                            5 -> 5
                            6 -> 12
                            7 -> 13
                            8 -> 2
                            9 -> 3
                            10 -> 10
                            11 -> 11
                            12 -> 6
                            13 -> 7
                            14 -> 14
                            15 -> 15
                            else -> -1
                        }

                        val to = if (c > 1) PATH + "c${fact}_${c}.png" else PATH + "c${fact}.png"
                        logger.warn("rename $fpng -> $to")
                        fpng.renameTo(File(to))
                        fdata.deleteOnExit()
                    }
                }
            }
        }
    }
}