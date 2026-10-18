package com.faces.core

import com.faces.core.model.FaceConfig
import com.faces.core.model.FaceStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class FaceConfigTest {

    @Test
    fun testDefaultColorsForStyles() {
        for (style in FaceStyle.entries) {
            val colors = FaceConfig.defaultColorsForStyle(style)
            org.junit.Assert.assertFalse(colors.isEmpty())
        }
    }

    @Test
    fun testAmbientModeOverrides() {
        val config = FaceConfig(style = FaceStyle.BUSINESS_TIME)

        // In ambient mode, background must be true black (0xFF000000) for AMOLED power saving
        assertEquals(0xFF000000.toInt(), config.getBackgroundColor(isAmbient = true))

        // In ambient mode, foreground hands must be white (0xFFFFFFFF)
        assertEquals(0xFFFFFFFF.toInt(), config.getHourColor(isAmbient = true))
        assertEquals(0xFFFFFFFF.toInt(), config.getMinuteColor(isAmbient = true))

        // Second hand must be transparent (hidden) in ambient mode
        assertEquals(0x00000000, config.getSecondColor(isAmbient = true))
    }
}
