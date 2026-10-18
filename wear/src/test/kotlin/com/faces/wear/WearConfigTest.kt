package com.faces.wear

import com.faces.core.model.FaceConfig
import com.faces.core.model.FaceStyle
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
}
