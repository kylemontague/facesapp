package watch.facesapp.core.mesh

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import java.util.Random
import androidx.core.graphics.createBitmap

object Trianglify {

    fun generatePoints(
        width: Float,
        height: Float,
        cellSize: Float = 70f,
        variance: Float = 25f,
        seed: Long = 42L
    ): List<Point> {
        val points = mutableListOf<Point>()
        val random = Random(seed)

        val bleed = cellSize * 0.5f
        var x = -bleed
        while (x <= width + bleed) {
            var y = -bleed
            while (y <= height + bleed) {
                val jx = x + (random.nextFloat() - 0.5f) * variance * 2f
                val jy = y + (random.nextFloat() - 0.5f) * variance * 2f
                points.add(Point(jx, jy))
                y += cellSize
            }
            x += cellSize
        }
        return points
    }

    fun triangulate(points: List<Point>, width: Float, height: Float): List<Triangle> {
        if (points.size < 3) return emptyList()

        val superTriMargin = maxOf(width, height) * 3f
        val p1 = Point(width / 2f, -superTriMargin)
        val p2 = Point(-superTriMargin, height + superTriMargin)
        val p3 = Point(width + superTriMargin, height + superTriMargin)
        val superTriangle = Triangle(p1, p2, p3)

        val triangulation = mutableListOf(superTriangle)

        for (point in points) {
            val badTriangles = mutableListOf<Triangle>()
            for (triangle in triangulation) {
                if (triangle.circumcircleContains(point)) {
                    badTriangles.add(triangle)
                }
            }

            val polygon = mutableListOf<Edge>()
            for (triangle in badTriangles) {
                for (edge in triangle.edges) {
                    var shared = false
                    for (other in badTriangles) {
                        if (triangle != other && other.edges.any { it.equalsEdge(edge) }) {
                            shared = true
                            break
                        }
                    }
                    if (!shared) {
                        polygon.add(edge)
                    }
                }
            }

            triangulation.removeAll(badTriangles)

            for (edge in polygon) {
                triangulation.add(Triangle(edge.p1, edge.p2, point))
            }
        }

        triangulation.removeAll {
            it.containsVertex(p1) || it.containsVertex(p2) || it.containsVertex(p3)
        }

        return triangulation
    }

    fun blendARGB(color1: Int, color2: Int, ratio: Float): Int {
        val r = ratio.coerceIn(0f, 1f)
        val ir = 1f - r
        val a = ((color1 ushr 24 and 0xff) * ir + (color2 ushr 24 and 0xff) * r).toInt()
        val red = ((color1 ushr 16 and 0xff) * ir + (color2 ushr 16 and 0xff) * r).toInt()
        val g = ((color1 ushr 8 and 0xff) * ir + (color2 ushr 8 and 0xff) * r).toInt()
        val b = ((color1 and 0xff) * ir + (color2 and 0xff) * r).toInt()
        return (a shl 24) or (red shl 16) or (g shl 8) or b
    }

    fun interpolateColor(colors: List<Int>, factor: Float): Int {
        if (colors.isEmpty()) return 0xFF000000.toInt()
        if (colors.size == 1) return colors[0]

        val clamped = factor.coerceIn(0f, 1f)
        val segment = 1f / (colors.size - 1)
        val index = (clamped / segment).toInt().coerceAtMost(colors.size - 2)
        val localFactor = (clamped - index * segment) / segment

        return blendARGB(colors[index], colors[index + 1], localFactor)
    }

    private val triangleCache = java.util.concurrent.ConcurrentHashMap<Long, List<Triangle>>()

    fun getOrComputeTriangles(width: Int, height: Int, seed: Long = 42L): List<Triangle> {
        val key = (width.toLong() shl 32) or (height.toLong() and 0xFFFFFFFFL) xor seed
        return triangleCache.getOrPut(key) {
            val points = generatePoints(width.toFloat(), height.toFloat(), seed = seed)
            triangulate(points, width.toFloat(), height.toFloat())
        }
    }

    fun renderToBitmap(
        width: Int,
        height: Int,
        colors: List<Int>,
        seed: Long = 42L
    ): Bitmap {
        val w = width.coerceAtLeast(1)
        val h = height.coerceAtLeast(1)
        // RGB_565 cuts memory footprint by 50% compared to ARGB_8888 for opaque background mesh
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)

        val triangles = getOrComputeTriangles(w, h, seed)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            isDither = true
        }

        val path = Path()
        val maxDist = (w + h).toFloat()

        for (tri in triangles) {
            val factor = (tri.centroid.x + tri.centroid.y) / maxDist
            paint.color = interpolateColor(colors, factor)

            path.reset()
            path.moveTo(tri.a.x, tri.a.y)
            path.lineTo(tri.b.x, tri.b.y)
            path.lineTo(tri.c.x, tri.c.y)
            path.close()

            canvas.drawPath(path, paint)
        }

        return bitmap
    }
}
