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
import kotlin.math.pow
import kotlin.math.sqrt

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
        val points: ArrayList<Point> = ArrayList<Point>()

        val gen = Random()
        val part = 512.0

        for (x in 0 until (IMG_SIZE/part).toInt() + 2 ) {
            for (y in 0 until (IMG_SIZE/part).toInt() + 2  ) {
                val px = (x - (gen.nextDouble()) ) * part
                val py = (y - (gen.nextDouble()) ) * part
                points.add(Point(px, py))
            }
        }

        val diagram = Voronoi(points)

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

        println("make layer sand")
        for (e in diagram.edges.subList(5,diagram.edges.size-1)) {
            drawLine(image, e.start.x, e.start.y, e.end.x, e.end.y, 64, SAND)
        }

        println("make layer water")
        for (e in diagram.edges.subList(5,diagram.edges.size-1)) {
            drawLine(image, e.start.x, e.start.y, e.end.x, e.end.y, 50, WATER)
        }

        println("make layer water deep")
        for (e in diagram.edges.subList(5,diagram.edges.size-1)) {
            drawLine(image, e.start.x, e.start.y, e.end.x, e.end.y, 36, WATER_DEEP)
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
    fun drawCircle(img: BufferedImage, x:Int , y:Int, radius:Int, color: Int) {
        var g = img.graphics
        g.setColor(Color(color))
        g.fillOval(x - radius/2, y - radius/2, radius, radius)
    }

    @JvmStatic
    fun drawLine(img: BufferedImage, x0:Double , y0:Double, x1:Double , y1:Double , size:Int, color: Int){

        var px = (x1 - x0)
        var py = (y1 - y0)

        val len2 = px.pow(2.0) + py.pow(2.0)
        val len = sqrt(len2)

        px /= len
        py /= len

        if(px == 0.0 || py == 0.0)
            return

        var dx = 0.0
        var dy = 0.0

        var i = 0

        while ( dx.pow(2.0) + dy.pow(2.0) < len2 ){

            if(i > IMG_SIZE*2)
                return

            val nx = (x0 + dx).toInt()
            val ny = (y0 + dy).toInt()

            if(nx >= 0&& ny >=0 && nx < IMG_SIZE && ny < IMG_SIZE){
                drawCircle(img, nx, ny, size, color)
            }
            dx += px
            dy += py

            i++
        }
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
