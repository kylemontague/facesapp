package watch.facesapp.core

import watch.facesapp.core.model.FaceStyle
import watch.facesapp.core.util.FaceShareUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceShareUtilsTest {

    @Test
    fun testCreateAndParseWebShareUrl() {
        val style = FaceStyle.FILL_TIME
        val colors = listOf(0xFF89C4BB.toInt(), 0xFFCDE9E6.toInt())
        val name = "Emerald Wave"

        val url = FaceShareUtils.createShareUrl(style, colors, name)
        assertTrue(url.startsWith("https://www.facesapp.watch/share?"))
        assertTrue(url.contains("style=FILL_TIME"))
        assertTrue(url.contains("colors=FF89C4BB,FFCDE9E6"))
        assertTrue(url.contains("name=Emerald+Wave") || url.contains("name=Emerald%20Wave"))

        val parsed = FaceShareUtils.parseShareUri(url)
        assertNotNull(parsed)
        assertEquals(FaceStyle.FILL_TIME, parsed!!.style)
        assertEquals(2, parsed.colors.size)
        assertEquals(0xFF89C4BB.toInt(), parsed.colors[0])
        assertEquals(0xFFCDE9E6.toInt(), parsed.colors[1])
        assertEquals("Emerald Wave", parsed.name)
    }

    @Test
    fun testCreateAndParseCustomSchemeUrl() {
        val style = FaceStyle.BUSINESS_TIME
        val colors = listOf(0xFF131947.toInt(), 0xFFC7FD00.toInt())
        val name = "Neon Executive"

        val url = FaceShareUtils.createCustomSchemeUrl(style, colors, name)
        assertTrue(url.startsWith("facesapp://share?"))

        val parsed = FaceShareUtils.parseShareUri(url)
        assertNotNull(parsed)
        assertEquals(FaceStyle.BUSINESS_TIME, parsed!!.style)
        assertEquals(2, parsed.colors.size)
        assertEquals(0xFF131947.toInt(), parsed.colors[0])
        assertEquals(0xFFC7FD00.toInt(), parsed.colors[1])
        assertEquals("Neon Executive", parsed.name)
    }

    @Test
    fun testParseInvalidUrlReturnsNull() {
        assertNull(FaceShareUtils.parseShareUri("https://google.com"))
        assertNull(FaceShareUtils.parseShareUri("invalid_string"))
        assertNull(FaceShareUtils.parseShareUri("https://facesapp.watch/other"))
    }

    @Test
    fun testFallbackToDefaultColorsIfEmpty() {
        val url = "https://www.facesapp.watch/share?style=PRISM_TIME"
        val parsed = FaceShareUtils.parseShareUri(url)
        assertNotNull(parsed)
        assertEquals(FaceStyle.PRISM_TIME, parsed!!.style)
        assertTrue(parsed.colors.isNotEmpty())
    }
}
