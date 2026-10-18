package watch.facesapp.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.core.model.FaceStyle

class ComplicationsTest {

    @Test
    fun testDefaultComplicationCycle() {
        assertEquals(listOf(FaceMode.TIME, FaceMode.DATE), FaceMode.DEFAULT_CYCLE)
    }

    @Test
    fun testCyclingDefaultSequence() {
        val cycle = FaceMode.DEFAULT_CYCLE // [TIME, DATE]

        assertEquals(FaceMode.DATE, FaceMode.TIME.next(cycle))
        assertEquals(FaceMode.TIME, FaceMode.DATE.next(cycle))
    }

    @Test
    fun testCyclingCustomSequence() {
        val customCycle = listOf(FaceMode.TIME, FaceMode.BATTERY, FaceMode.STEPS, FaceMode.HEART_RATE)

        assertEquals(FaceMode.BATTERY, FaceMode.TIME.next(customCycle))
        assertEquals(FaceMode.STEPS, FaceMode.BATTERY.next(customCycle))
        assertEquals(FaceMode.HEART_RATE, FaceMode.STEPS.next(customCycle))
        assertEquals(FaceMode.TIME, FaceMode.HEART_RATE.next(customCycle))
    }

    @Test
    fun testCyclingWhenCurrentModeNotInCycle() {
        val customCycle = listOf(FaceMode.TIME, FaceMode.BATTERY)

        // If currently on STEPS (which was disabled), next should wrap to the first item (TIME)
        assertEquals(FaceMode.TIME, FaceMode.STEPS.next(customCycle))
    }

    @Test
    fun testFaceConfigDefaultActiveComplications() {
        val config = FaceConfig(
            id = "test-1",
            style = FaceStyle.OUA_TIME
        )

        assertEquals(listOf(FaceMode.TIME, FaceMode.DATE), config.activeComplications)
    }

    @Test
    fun testFaceConfigWithComplications() {
        val initial = FaceConfig(id = "test-2", style = FaceStyle.BUSINESS_TIME)
        val updated = initial.withComplications(
            listOf(FaceMode.TIME, FaceMode.BATTERY, FaceMode.CALORIES)
        )

        assertEquals("time,battery,calories", updated.options["complications"])
        assertEquals(
            listOf(FaceMode.TIME, FaceMode.BATTERY, FaceMode.CALORIES),
            updated.activeComplications
        )
    }

    @Test
    fun testFaceConfigParseCorruptComplicationsFallback() {
        val config = FaceConfig(
            id = "test-3",
            style = FaceStyle.FILL_TIME,
            options = mapOf("complications" to "invalid_id,non_existent")
        )

        // Invalid IDs are filtered out, fallback ensures DEFAULT_CYCLE is returned
        assertEquals(listOf(FaceMode.TIME, FaceMode.DATE), config.activeComplications)
    }

    @Test
    fun testFaceConfigEnsureTimePrependedAtPositionZero() {
        val config = FaceConfig(
            id = "test-4",
            style = FaceStyle.FILL_TIME,
            options = mapOf("complications" to "battery,steps")
        )

        // TIME must be guaranteed at position 0 even if absent from serialized options
        assertEquals(
            listOf(FaceMode.TIME, FaceMode.BATTERY, FaceMode.STEPS),
            config.activeComplications
        )
    }
}
