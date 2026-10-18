package watch.facesapp.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import watch.facesapp.core.model.ContrastConstraint
import watch.facesapp.core.model.FaceStyle
import watch.facesapp.core.util.ColorContrast

class ColorContrastTest {

    private val white = 0xFFFFFFFF.toInt()
    private val black = 0xFF000000.toInt()
    private val darkLuxury = 0xFF080A0D.toInt()
    private val yellow = 0xFFFFFF00.toInt()
    private val navy = 0xFF001F3F.toInt()

    @Test
    fun testCalculateContrastExtremeValues() {
        val contrastMax = ColorContrast.calculateContrast(white, black)
        assertEquals(21.0, contrastMax, 0.01)

        val contrastMin = ColorContrast.calculateContrast(black, black)
        assertEquals(1.0, contrastMin, 0.01)
    }

    @Test
    fun testMeetsContrast() {
        assertTrue(ColorContrast.meetsContrast(white, darkLuxury, 3.0))
        assertTrue(ColorContrast.meetsContrast(white, darkLuxury, 4.5))
        assertFalse(ColorContrast.meetsContrast(black, darkLuxury, 3.0))
    }

    @Test
    fun testGetContrastingTextColor() {
        assertEquals(white, ColorContrast.getContrastingTextColor(darkLuxury))
        assertEquals(darkLuxury, ColorContrast.getContrastingTextColor(white))
        assertEquals(darkLuxury, ColorContrast.getContrastingTextColor(yellow))
    }

    @Test
    fun testTemplateContrastConstraintsDefined() {
        val ouatConstraints = FaceStyle.OUA_TIME.template.contrastConstraints
        assertTrue(ouatConstraints.isNotEmpty())
        assertEquals("HR", ouatConstraints[0].foregroundRole)
        assertEquals("BG", ouatConstraints[0].backgroundRole)
        assertEquals(3.5, ouatConstraints[0].minRatio, 0.01)

        val businessConstraints = FaceStyle.BUSINESS_TIME.template.contrastConstraints
        assertEquals(2, businessConstraints.size)
        assertTrue(businessConstraints.any { it.foregroundRole == "HR" && it.backgroundRole == "BG" })
        assertTrue(businessConstraints.any { it.foregroundRole == "MIN" && it.backgroundRole == "BG" })

        assertTrue(FaceStyle.TWO_TIME.template.contrastConstraints.isNotEmpty())
        assertTrue(FaceStyle.FILL_TIME.template.contrastConstraints.isNotEmpty())
        assertTrue(FaceStyle.PRISM_TIME.template.contrastConstraints.isNotEmpty())
    }

    @Test
    fun testIsSwatchValidForRole() {
        val constraints = listOf(
            ContrastConstraint("TXT", "BG", minRatio = 3.0)
        )

        val currentAssignments = mapOf(
            "BG" to darkLuxury,
            "TXT" to white
        )

        // Testing assigning foreground "TXT"
        assertTrue(ColorContrast.isSwatchValidForRole(white, "TXT", currentAssignments, constraints))
        assertFalse(ColorContrast.isSwatchValidForRole(darkLuxury, "TXT", currentAssignments, constraints))

        // Testing assigning background "BG"
        assertTrue(ColorContrast.isSwatchValidForRole(navy, "BG", currentAssignments, constraints))
        assertFalse(ColorContrast.isSwatchValidForRole(white, "BG", currentAssignments, constraints)) // white on white fails
    }

    @Test
    fun testSatisfiesAllConstraints() {
        val constraints = listOf(
            ContrastConstraint("HR", "BG", minRatio = 3.0),
            ContrastConstraint("MIN", "BG", minRatio = 3.0)
        )

        val validAssignments = mapOf(
            "BG" to darkLuxury,
            "HR" to white,
            "MIN" to yellow
        )
        assertTrue(ColorContrast.satisfiesAllConstraints(validAssignments, constraints))

        val invalidAssignments = mapOf(
            "BG" to darkLuxury,
            "HR" to black,
            "MIN" to white
        )
        assertFalse(ColorContrast.satisfiesAllConstraints(invalidAssignments, constraints))
    }

    @Test
    fun testCalculateCumulativeScore() {
        val constraints = listOf(
            ContrastConstraint("TXT", "BG", minRatio = 3.0)
        )

        val highScoreAssignments = mapOf("TXT" to white, "BG" to black)
        val lowScoreAssignments = mapOf("TXT" to darkLuxury, "BG" to black)

        val highScore = ColorContrast.calculateCumulativeScore(highScoreAssignments, constraints)
        val lowScore = ColorContrast.calculateCumulativeScore(lowScoreAssignments, constraints)

        assertTrue(highScore > lowScore)
    }
}
