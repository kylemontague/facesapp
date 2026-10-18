package com.faces.mobile

import com.faces.core.model.FaceConfig
import com.faces.core.model.FaceStyle
import com.faces.mobile.data.db.Converters
import com.faces.mobile.data.db.FaceEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceEntityTest {

    private val converters = Converters()

    @Test
    fun testEntityToConfigConversion() {
        val entity = FaceEntity(
            id = "test-123",
            style = FaceStyle.FILL_TIME,
            colors = listOf(0xFF112233.toInt(), 0xFF445566.toInt()),
            name = "My Fill Face",
            isFavorite = true,
            createdAt = 123456789L
        )

        val config = entity.toFaceConfig()
        assertEquals("test-123", config.id)
        assertEquals(FaceStyle.FILL_TIME, config.style)
        assertEquals(2, config.colors.size)
        assertEquals(0xFF112233.toInt(), config.colors[0])
        assertTrue(config.isFavorite)
        assertEquals(123456789L, config.createdAt)
    }

    @Test
    fun testConfigToEntityConversion() {
        val config = FaceConfig(
            id = "config-456",
            style = FaceStyle.BUSINESS_TIME,
            colors = listOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt()),
            isFavorite = false,
            createdAt = 987654321L
        )

        val entity = FaceEntity.fromFaceConfig(config, name = "Work Watch")
        assertEquals("config-456", entity.id)
        assertEquals(FaceStyle.BUSINESS_TIME, entity.style)
        assertEquals("Work Watch", entity.name)
        assertFalse(entity.isFavorite)
        assertEquals(987654321L, entity.createdAt)
    }

    @Test
    fun testStockPresets() {
        val presets = FaceEntity.createStockPresets()
        assertEquals(FaceStyle.entries.size, presets.size)

        for (preset in presets) {
            assertTrue(preset.colors.isNotEmpty())
            assertFalse(preset.isFavorite)
        }
    }

    @Test
    fun testColorListConverter() {
        val colors = listOf(0xFF080A0D.toInt(), 0xFFFFFFFF.toInt(), 0xFFC7FD00.toInt())
        val serialized = converters.fromColorList(colors)
        val deserialized = converters.toColorList(serialized)

        assertEquals(colors, deserialized)
    }

    @Test
    fun testStyleConverter() {
        val style = FaceStyle.PRISM_TIME
        val str = converters.fromFaceStyle(style)
        val deserialized = converters.toFaceStyle(str)

        assertEquals(style, deserialized)
    }
}
