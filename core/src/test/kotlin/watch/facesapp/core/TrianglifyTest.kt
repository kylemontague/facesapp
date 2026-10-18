package watch.facesapp.core

import watch.facesapp.core.mesh.Point
import watch.facesapp.core.mesh.Trianglify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrianglifyTest {

    @Test
    fun testPointGeneration() {
        val points = Trianglify.generatePoints(width = 300f, height = 300f, cellSize = 80f)
        assertTrue("Generated points should not be empty", points.isNotEmpty())
        assertTrue("Should generate at least 16 points for a 300x300 grid", points.size >= 16)
    }

    @Test
    fun testTriangulation() {
        val points = listOf(
            Point(0f, 0f),
            Point(100f, 0f),
            Point(100f, 100f),
            Point(0f, 100f),
            Point(50f, 50f)
        )
        val triangles = Trianglify.triangulate(points, 100f, 100f)
        assertFalse("Triangulation should produce triangles", triangles.isEmpty())
        for (tri in triangles) {
            assertTrue("Triangle vertices must be distinct", tri.a != tri.b && tri.b != tri.c)
        }
    }

    @Test
    fun testColorInterpolation() {
        val colors = listOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
        val startColor = Trianglify.interpolateColor(colors, 0f)
        val endColor = Trianglify.interpolateColor(colors, 1f)
        assertEquals(0xFF000000.toInt(), startColor)
        assertEquals(0xFFFFFFFF.toInt(), endColor)
    }
}
