package com.origin.utils

import com.origin.utils.TileColors.CLAY
import com.origin.utils.TileColors.FOREST_LEAF
import com.origin.utils.TileColors.FOREST_PINE
import com.origin.utils.TileColors.MEADOW_LOW
import com.origin.utils.TileColors.PRAIRIE
import com.origin.utils.TileColors.SWAMP
import com.origin.utils.TileColors.TUNDRA
import com.origin.utils.TileColors.WATER_DEEP
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.security.SecureRandom
import javax.imageio.ImageIO

const val IMG_SIZE = SUPERGRID_SIZE * GRID_SIZE

@Suppress("UsePropertyAccessSyntax")
val BLACK_COLOR: Int = Color.black.getRGB()

val rnd = SecureRandom()

object MapGenerator {
    var isMakeLakes = false
    var isMakeTiles = false
    var isNewImage = false

    @JvmStatic
    fun main(args: Array<String>) {
        println("map generator start...")
        args.forEach {
            if (it == "lakes") isMakeLakes = true
            if (it == "tiles") isMakeTiles = true
            if (it == "new") isNewImage = true
        }

        val image: BufferedImage =
            if (isNewImage) {
                println("create new map.png")
                BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_RGB)
            } else {
                println("load from map.png")
                val f = File("map.png")
                ImageIO.read(f)
            }

        if (isMakeLakes) {
            println("make lakes...")
            layer(image, 250.0, 0.69, WATER_DEEP)
            layer(image, 280.0, 0.73, WATER_DEEP)
        }
        if (isMakeTiles) {
            println("make tiles...")
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
        }

        val f = File("map.png")
        println("save to file")
        try {
            ImageIO.write(image, "png", f)
        } catch (e: Exception) {
            println("error ${e.message}")
        }
        println("done")
    }

    @JvmStatic
    fun layer(img: BufferedImage, div: Double, threshold: Double, color: Int) {
        val noise = OpenSimplexNoise(rnd.nextLong())

        println("make layer $div $threshold $color")
        for (x in 0 until IMG_SIZE) {
            for (y in 0 until IMG_SIZE) {
                val oc = img.getRGB(x, y)
                if (oc == BLACK_COLOR || !isNewImage) {
                    val v = noise.eval(x.toDouble() / div, y.toDouble() / div)
                    img.setRGB(x, y, if (v > threshold) color else (if (isNewImage) 0 else oc))
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
