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
    var isMakeLakes = false
    var isMakeTiles = false
    var isNewImage = false

    val gen = Random()

    class Vector(var x: Double, var y: Double){

        fun rotate(deg: Double){
            x = x * cos(deg * 180.0/Math.PI ) - y * sin(deg * 180.0/Math.PI)
            y = x * sin(deg * 180.0/Math.PI ) + y * cos(deg * 180.0/Math.PI)
        }

        fun dist(): Double{
            return sqrt(x.pow(2.0) + y.pow(2.0))
        }

        fun dist2(): Double{
            return x.pow(2.0) + y.pow(2.0)
        }

        fun nor(){
            val dist = dist()
            x /= dist
            y /= dist
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("map generator start...")
        args.forEach {
            if (it == "lakes") isMakeLakes = true
            if (it == "tiles") isMakeTiles = true
            if (it == "new") isNewImage = true
        }
        val points: ArrayList<Point> = ArrayList<Point>()

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

        val pointsToDraw: MutableList<MutableList<Vector>> = mutableListOf<MutableList<Vector>>()
        val sizePointsToWater: MutableList<MutableList<Int>> = mutableListOf<MutableList<Int>>()
        val sizePointsToDeepWater: MutableList<MutableList<Int>> = mutableListOf<MutableList<Int>>()
        val sizePointsToSand: MutableList<MutableList<Int>> = mutableListOf<MutableList<Int>>()

        for (e in diagram.edges.subList(5,diagram.edges.size-1)) {

            pointsToDraw.add(mutableListOf<Vector>())
            sizePointsToWater.add(mutableListOf<Int>())
            sizePointsToDeepWater.add(mutableListOf<Int>())
            sizePointsToSand.add(mutableListOf<Int>())

            val view = Vector(e.end.x - e.start.x, e.end.y - e.start.y)
            val dist2 = view.dist2()
            view.nor()

            val dv = Vector(0.0,0.0)

            var i = 0

            while ( dv.dist2() < dist2 ) {

                if(i > IMG_SIZE*2)
                    break

                view.rotate((rnd.nextDouble() - 0.5) * 0.0002 )

                dv.x += view.x
                dv.y += view.y

                pointsToDraw[pointsToDraw.size-1].add(Vector(dv.x + e.start.x, dv.y + e.start.y))
                sizePointsToWater[pointsToDraw.size-1].add((gen.nextDouble()*45.0 + 20).toInt())
                sizePointsToDeepWater[pointsToDraw.size-1].add((gen.nextDouble()*30.0 + 10).toInt())
                sizePointsToSand[pointsToDraw.size-1].add((gen.nextDouble()*75.0).toInt())

                i++
            }

        }

        var idx = 0
        println("make layer sand")
        for (e in diagram.edges.subList(5,diagram.edges.size-1)) {
            if(gen.nextDouble() > 0.9)
                drawLine(image, pointsToDraw[idx], sizePointsToSand[idx], SAND)
            idx ++
        }

        idx = 0
        println("make layer water")
        for (e in diagram.edges.subList(5,diagram.edges.size-1)) {
            drawLine(image, pointsToDraw[idx], sizePointsToWater[idx], WATER)
            idx ++
        }

        idx = 0
        println("make layer water deep")
        for (e in diagram.edges.subList(5,diagram.edges.size-1)) {
            drawLine(image, pointsToDraw[idx], sizePointsToDeepWater[idx], WATER_DEEP)
            idx ++
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
    fun drawLine(img: BufferedImage , pointsToDraw:MutableList<Vector>, sizePointsToDraw:MutableList<Int>, color: Int){
        
        var idx = 0
        for( p in pointsToDraw){
            if(p.x >= 0&& p.y >=0 && p.x < IMG_SIZE && p.y < IMG_SIZE){
                drawCircle(img, p.x.toInt(), p.y.toInt(), sizePointsToDraw[idx++], color)
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
