package com.origin.utils

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.security.SecureRandom
import javax.imageio.ImageIO


val IMG_SIZE = 5000

@Suppress("UsePropertyAccessSyntax")
val BLACK_COLOR: Int = Color.black.getRGB()

object MapGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        println("map generator start...")

        val image = BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_RGB)

        layer(image, 100.0, 0.4, 0x0000ff)
        layer(image, 170.0, 0.6, 0x00ff00)
        layer(image, 170.0, 0.6, 0xffff00)
        layer(image, 300.0, 0.0, 0xe07b39)

        fill(image, 0x333333)

        val f = File("map.png")
        println("save to file")
        ImageIO.write(image, "png", f)
        println("done")
    }

    fun layer(img: BufferedImage, div: Double, threshold: Double, color: Int) {
        val rnd = SecureRandom()
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