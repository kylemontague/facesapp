package watch.facesapp.wear

import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WearConfigTest {

    @Test
    fun testPayloadParsing() {
        val payload = "FILL_TIME|-16777216,-1,-65536"
        val parts = payload.split("|")
        val styleId = parts[0]
        val colors = parts[1].split(",").mapNotNull { it.trim().toIntOrNull() }

        val config = FaceConfig(
            style = FaceStyle.fromId(styleId),
            colors = colors
        )

        assertEquals(FaceStyle.FILL_TIME, config.style)
        assertEquals(3, config.colors.size)
        assertEquals(-16777216, config.colors[0])
    }

    @Test
    fun testFallbackDefaultColors() {
        val style = FaceStyle.PRISM_TIME
        val colors = FaceConfig.defaultColorsForStyle(style)
        assertTrue(colors.isNotEmpty())
    }

    @Test
    fun testDynamicRefreshRate() {
        // OUAT: 1000ms in active TIME mode, 60,000ms in ambient or complications
        assertEquals(1000L, watch.facesapp.wear.renderer.FacesCanvasRenderer.calculateUpdateDelayMillis(FaceStyle.OUA_TIME, watch.facesapp.core.model.FaceMode.TIME, isAmbient = false))
        assertEquals(60_000L, watch.facesapp.wear.renderer.FacesCanvasRenderer.calculateUpdateDelayMillis(FaceStyle.OUA_TIME, watch.facesapp.core.model.FaceMode.TIME, isAmbient = true))
        assertEquals(60_000L, watch.facesapp.wear.renderer.FacesCanvasRenderer.calculateUpdateDelayMillis(FaceStyle.OUA_TIME, watch.facesapp.core.model.FaceMode.STEPS, isAmbient = false))

        // Business Time: 1000ms in active TIME mode, 60,000ms in ambient or complications
        assertEquals(1000L, watch.facesapp.wear.renderer.FacesCanvasRenderer.calculateUpdateDelayMillis(FaceStyle.BUSINESS_TIME, watch.facesapp.core.model.FaceMode.TIME, isAmbient = false))
        assertEquals(60_000L, watch.facesapp.wear.renderer.FacesCanvasRenderer.calculateUpdateDelayMillis(FaceStyle.BUSINESS_TIME, watch.facesapp.core.model.FaceMode.BATTERY, isAmbient = false))

        // Two Time & Prism: Always 60,000ms (no seconds representation)
        assertEquals(60_000L, watch.facesapp.wear.renderer.FacesCanvasRenderer.calculateUpdateDelayMillis(FaceStyle.TWO_TIME, watch.facesapp.core.model.FaceMode.TIME, isAmbient = false))
        assertEquals(60_000L, watch.facesapp.wear.renderer.FacesCanvasRenderer.calculateUpdateDelayMillis(FaceStyle.PRISM_TIME, watch.facesapp.core.model.FaceMode.TIME, isAmbient = false))
    }
}
