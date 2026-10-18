package watch.facesapp.core

import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceStyle
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

    @Test
    fun testPrismColorRoleResolution() {
        val prismColors = listOf(
            0xFF112233.toInt(), // BG: Mesh Primary
            0xFF445566.toInt(), // ACC: Mesh Secondary
            0xFF778899.toInt()  // TXT: Time Text
        )
        val config = FaceConfig(
            style = FaceStyle.PRISM_TIME,
            colors = prismColors
        )

        assertEquals(0xFF112233.toInt(), config.getBackgroundColor(isAmbient = false))
        assertEquals(0xFF445566.toInt(), config.getAccentColor(isAmbient = false))
        assertEquals(0xFF778899.toInt(), config.getTextColor(isAmbient = false))

        // Role-based direct access
        assertEquals(0xFF112233.toInt(), config.getColorForRole("BG"))
        assertEquals(0xFF445566.toInt(), config.getColorForRole("ACC"))
        assertEquals(0xFF778899.toInt(), config.getColorForRole("TXT"))

        // Prism roles size must be 3
        assertEquals(3, FaceStyle.PRISM_TIME.template.colorRoles.size)
        assertEquals(listOf("BG", "ACC", "TXT"), FaceStyle.PRISM_TIME.template.colorRoles.map { it.code })
    }
}
