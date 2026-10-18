package com.faces.mobile

import com.faces.core.model.FaceConfig
import com.faces.core.model.FaceStyle
import com.faces.mobile.data.db.Converters
import com.faces.mobile.data.db.FaceEntity
import com.faces.mobile.data.db.FaceWearLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
            createdAt = 123456789L,
            lastWornAt = 123456789L,
            isCurrentOnWatch = true
        )

        val config = entity.toFaceConfig()
        assertEquals("test-123", config.id)
        assertEquals(FaceStyle.FILL_TIME, config.style)
        assertEquals(2, config.colors.size)
        assertEquals(0xFF112233.toInt(), config.colors[0])
        assertTrue(config.isFavorite)
        assertEquals(123456789L, config.createdAt)
        assertTrue(entity.isCurrentOnWatch)
        assertNotNull(entity.lastWornAt)
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

        val entity = FaceEntity.fromFaceConfig(config, name = "Work Watch", lastWornAt = 987654321L, isCurrentOnWatch = true)
        assertEquals("config-456", entity.id)
        assertEquals(FaceStyle.BUSINESS_TIME, entity.style)
        assertEquals("Work Watch", entity.name)
        assertFalse(entity.isFavorite)
        assertEquals(987654321L, entity.createdAt)
        assertTrue(entity.isCurrentOnWatch)
    }

    @Test
    fun testStockPresets() {
        val presets = FaceEntity.createStockPresets()
        assertEquals(FaceStyle.entries.size, presets.size)

        // First stock preset is marked as currently on watch
        assertTrue(presets.first().isCurrentOnWatch)
        assertNotNull(presets.first().lastWornAt)

        for (preset in presets) {
            assertTrue(preset.colors.isNotEmpty())
            assertFalse(preset.isFavorite)
        }
    }

    @Test
    fun testWearLogEntity() {
        val log = FaceWearLogEntity(
            faceId = "face-123",
            wornAt = 1000L,
            wornUntil = 2000L
        )
        assertEquals("face-123", log.faceId)
        assertEquals(1000L, log.wornAt)
        assertEquals(2000L, log.wornUntil)
    }

    @Test
    fun testDurationFormatting() {
        assertEquals("< 1m", com.faces.mobile.ui.home.formatDuration(30_000L))
        assertEquals("15m", com.faces.mobile.ui.home.formatDuration(15 * 60 * 1000L))
        assertEquals("2h 30m", com.faces.mobile.ui.home.formatDuration((2 * 3600 + 30 * 60) * 1000L))
        assertEquals("3d 4h", com.faces.mobile.ui.home.formatDuration((3 * 86400 + 4 * 3600) * 1000L))
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
