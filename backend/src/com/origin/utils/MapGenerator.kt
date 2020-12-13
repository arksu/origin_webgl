package com.origin.utils

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.security.SecureRandom
import javax.imageio.ImageIO

val IMG_SIZE = 5000

@Suppress("UsePropertyAccessSyntax")
val BLACK_COLOR: Int = Color.black.getRGB()

val rnd = SecureRandom()

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


object MapGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        println("map generator start...")

        val image = BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_RGB)

        layer(image, 300.0, 0.6, WATER)
        layer(image, 400.0, 0.8, CLAY)
        layer(image, 170.0, 0.6, MEADOW_LOW)
        layer(image, 200.0, 0.1, FOREST_PINE)

        fill(image, FOREST_LEAF)

        val f = File("map.png")
        println("save to file")
        ImageIO.write(image, "png", f)
        println("done")
    }

    @JvmStatic
    fun layer(img: BufferedImage, div: Double, threshold: Double, color: Int) {
        val noise = OpenSimplexNoise(rnd.nextLong())

        println("make layer $div $threshold $color")
        for (x in 0 until IMG_SIZE) {
            for (y in 0 until IMG_SIZE) {
                val oc = img.getRGB(x, y)
                if (oc == BLACK_COLOR) {
                    val v = noise.eval(x.toDouble() / div, y.toDouble() / div)
                    val c: Int = if (v > threshold) color else 0
                    img.setRGB(x, y, c)
                }
            }
        }
    }

    @JvmStatic
    fun fill(img: BufferedImage, color: Int) {
        println("fill color")
        for (x in 0 until IMG_SIZE) {
            for (y in 0 until IMG_SIZE) {
                val oc = img.getRGB(x, y)
                if (oc == BLACK_COLOR) {
                    img.setRGB(x, y, color)
                }
            }
        }
    }
}