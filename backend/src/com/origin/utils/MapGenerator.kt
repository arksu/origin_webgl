package com.origin.utils

import com.origin.utils.TileColors.CLAY
import com.origin.utils.TileColors.FOREST_LEAF
import com.origin.utils.TileColors.FOREST_PINE
import com.origin.utils.TileColors.MEADOW_LOW
import com.origin.utils.TileColors.PRAIRIE
import com.origin.utils.TileColors.SAND
import com.origin.utils.TileColors.SWAMP
import com.origin.utils.TileColors.TUNDRA
import com.origin.utils.TileColors.WATER
import com.origin.utils.TileColors.WATER_DEEP
import com.origin.utils.voronoi.Point
import com.origin.utils.voronoi.Voronoi
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.security.SecureRandom
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

const val IMG_SIZE = SUPERGRID_SIZE * GRID_SIZE

@Suppress("UsePropertyAccessSyntax")
val BLACK_COLOR: Int = Color.black.getRGB()

val rnd = SecureRandom()

object MapGenerator {
    class Vector(var x: Double, var y: Double) {

        fun rotate(deg: Double) {
            x = x * cos(deg * 180.0 / Math.PI) - y * sin(deg * 180.0 / Math.PI)
            y = x * sin(deg * 180.0 / Math.PI) + y * cos(deg * 180.0 / Math.PI)
        }

        private fun dist(): Double {
            return sqrt(x.pow(2.0) + y.pow(2.0))
        }

        fun dist2(): Double {
            return x.pow(2.0) + y.pow(2.0)
        }

        fun nor() {
            val dist = dist()
            x /= dist
            y /= dist
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("map generator start...")

        val points: ArrayList<Point> = ArrayList<Point>()

        val part = 400.0

        for (x in 0 until (IMG_SIZE / part).toInt() + 2) {
            for (y in 0 until (IMG_SIZE / part).toInt() + 2) {
                val px = (x - (rnd.nextDouble())) * part
                val py = (y - (rnd.nextDouble())) * part
                points.add(Point(px, py))
            }
        }

        val diagram = Voronoi(points)

        val image = BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_RGB)


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

        val pointsToDraw: MutableList<MutableList<Vector>> = mutableListOf()
        val sizePointsToWater: MutableList<MutableList<Int>> = mutableListOf()
        val sizePointsToDeepWater: MutableList<MutableList<Int>> = mutableListOf()
        val sizePointsToSand: MutableList<MutableList<Int>> = mutableListOf()
        val dropEdges: MutableList<Boolean> = mutableListOf()

        for (e in diagram.edges.subList(5, diagram.edges.size - 1)) {

            pointsToDraw.add(mutableListOf())
            sizePointsToWater.add(mutableListOf())
            sizePointsToDeepWater.add(mutableListOf())
            sizePointsToSand.add(mutableListOf())
            dropEdges.add(rnd.nextDouble() <= 0.8)


            val view = Vector(e.end.x - e.start.x, e.end.y - e.start.y)
            val dist2 = view.dist2()
            view.nor()

            val dv = Vector(0.0, 0.0)

            var i = 0
            var delta = 0.0
            var integr = 0.0

            while (dv.dist2() < dist2) {

                if (i > IMG_SIZE * 2)
                    break

                view.rotate((rnd.nextDouble() - 0.5) * 0.0002)

                dv.x += view.x
                dv.y += view.y

                pointsToDraw[pointsToDraw.size - 1].add(Vector(dv.x + e.start.x, dv.y + e.start.y))

                if (i % 5 == 0) {
                    delta = (rnd.nextDouble() - 0.5) * 0.5
                }

                integr += delta

                sizePointsToWater[pointsToDraw.size - 1].add((rnd.nextDouble() * 10.0 + 25 + integr).toInt())
                sizePointsToDeepWater[pointsToDraw.size - 1].add((rnd.nextDouble() * 2.0 + 20 + integr).toInt())
                sizePointsToSand[pointsToDraw.size - 1].add((rnd.nextDouble() * 5.0 + integr + if (rnd.nextDouble() > 0.999) 40 else 0).toInt())

                i++
            }
        }

        var idx = 0
        println("make layer sand")
        for (e in diagram.edges.subList(5, diagram.edges.size - 1)) {
            // if(gen.nextDouble() > 0.9)
            if (dropEdges[idx])
                drawLine(image, pointsToDraw[idx], sizePointsToSand[idx], SAND)
            idx++
        }

        idx = 0
        println("make layer water")
        for (e in diagram.edges.subList(5, diagram.edges.size - 1)) {
            if (dropEdges[idx])
                drawLine(image, pointsToDraw[idx], sizePointsToWater[idx], WATER)
            idx++
        }

        val lakes: MutableList<Vector> = mutableListOf()
        val lakesSize: MutableList<Vector> = mutableListOf()

        idx = 0
        println("make layer lakes")
        for (e in diagram.edges.subList(5, diagram.edges.size - 1)) {
            if (rnd.nextDouble() > 0.96) {
                lakes.add(Vector(e.start.x, e.start.y))
                val randSize = (rnd.nextDouble() - 0.5) * 300
                lakesSize.add(
                    Vector(
                        rnd.nextDouble() * 50.0 + 150 + randSize,
                        rnd.nextDouble() * 50.0 + 150.0 + randSize
                    )
                )
            }
            idx++
        }

        idx = 0
        for (l in lakes) {
            drawCircle(image, l.x.toInt(), l.y.toInt(), lakesSize[idx].x.toInt(), lakesSize[idx].y.toInt(), WATER)
            idx++
        }

        idx = 0
        println("make layer water deep")
        for (e in diagram.edges.subList(5, diagram.edges.size - 1)) {
            if (dropEdges[idx])
                drawLine(image, pointsToDraw[idx], sizePointsToDeepWater[idx], WATER_DEEP)
            idx++
        }

        idx = 0
        for (l in lakes) {
            drawCircle(
                image,
                l.x.toInt(),
                l.y.toInt(),
                lakesSize[idx].x.toInt() - 15,
                lakesSize[idx].y.toInt() - 15,
                WATER_DEEP
            )
            idx++
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
    fun makeLake(img: BufferedImage, x: Int, y: Int, radius0: Int, radius1: Int) {
        val g = img.graphics
        g.color = Color(WATER)
        g.fillOval(x - radius0 / 2, y - radius1 / 2, radius0, radius1)
    }

    @JvmStatic
    fun drawCircle(img: BufferedImage, x: Int, y: Int, radius0: Int, radius1: Int, color: Int) {
        val g = img.graphics
        g.color = Color(color)
        g.fillOval(x - radius0 / 2, y - radius1 / 2, radius0, radius1)
    }

    @JvmStatic
    fun drawLine(
        img: BufferedImage,
        pointsToDraw: MutableList<Vector>,
        sizePointsToDraw: MutableList<Int>,
        color: Int
    ) {

        var idx = 0
        for (p in pointsToDraw) {
            if (p.x >= 0 && p.y >= 0 && p.x < IMG_SIZE && p.y < IMG_SIZE) {
                drawCircle(img, p.x.toInt(), p.y.toInt(), sizePointsToDraw[idx], sizePointsToDraw[idx], color)
                idx++
            }
        }
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
                    img.setRGB(x, y, if (v > threshold) color else oc)
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
