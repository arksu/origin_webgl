package com.origin.utils

import com.origin.utils.TileColors.CLAY
import com.origin.utils.TileColors.FOREST_LEAF
import com.origin.utils.TileColors.FOREST_PINE
import com.origin.utils.TileColors.MEADOW_LOW
import com.origin.utils.TileColors.PRAIRIE
import com.origin.utils.TileColors.SWAMP
import com.origin.utils.TileColors.TUNDRA
import com.origin.utils.TileColors.WATER
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.security.SecureRandom
import javax.imageio.ImageIO

val IMG_SIZE = SUPERGRID_SIZE * GRID_SIZE

@Suppress("UsePropertyAccessSyntax")
val BLACK_COLOR: Int = Color.black.getRGB()

val rnd = SecureRandom()

object MapGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        println("map generator start...")

        val image = BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_RGB)

        layer(image, 300.0, 0.64, WATER)
        layer(image, 330.0, 0.7, WATER)
        layer(image, 250.0, 0.74, SWAMP)
        layer(image, 300.0, 0.8, SWAMP)
        layer(image, 300.0, 0.76, CLAY)
        layer(image, 400.0, 0.8, CLAY)
        layer(image, 170.0, 0.6, MEADOW_LOW)
        layer(image, 220.0, 0.72, PRAIRIE)
        layer(image, 170.0, 0.8, TUNDRA)
        layer(image, 200.0, 0.1, FOREST_PINE)
        layer(image, 260.0, 0.2, FOREST_PINE)

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