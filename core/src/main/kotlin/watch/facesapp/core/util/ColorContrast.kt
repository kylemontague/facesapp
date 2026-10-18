package watch.facesapp.core.util

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object ColorContrast {

    /**
     * Calculates the WCAG 2.1 relative luminance of an sRGB color (0.0 to 1.0).
     */
    fun calculateLuminance(color: Int): Double {
        val r = ((color shr 16) and 0xFF) / 255.0
        val g = ((color shr 8) and 0xFF) / 255.0
        val b = (color and 0xFF) / 255.0

        val rLinear = if (r <= 0.03928) r / 12.92 else ((r + 0.055) / 1.055).pow(2.4)
        val gLinear = if (g <= 0.03928) g / 12.92 else ((g + 0.055) / 1.055).pow(2.4)
        val bLinear = if (b <= 0.03928) b / 12.92 else ((b + 0.055) / 1.055).pow(2.4)

        return 0.2126 * rLinear + 0.7152 * gLinear + 0.0722 * bLinear
    }

    /**
     * Computes the contrast ratio between two ARGB colors.
     * Returns a value between 1.0 (no contrast) and 21.0 (maximum contrast).
     */
    fun calculateContrast(foreground: Int, background: Int): Double {
        val lum1 = calculateLuminance(foreground)
        val lum2 = calculateLuminance(background)
        val lighter = max(lum1, lum2)
        val darker = min(lum1, lum2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    /**
     * Determines whether white or dark luxury black (#080a0d) provides higher contrast
     * against the given background color.
     */
    fun getContrastingTextColor(backgroundColor: Int): Int {
        val white = 0xFFFFFFFF.toInt()
        val dark = 0xFF080A0D.toInt()
        val contrastWithWhite = calculateContrast(white, backgroundColor)
        val contrastWithDark = calculateContrast(dark, backgroundColor)
        return if (contrastWithWhite >= contrastWithDark) white else dark
    }

    /**
     * Checks if two colors meet the WCAG AA minimum contrast ratio (4.5:1) for normal text.
     */
    fun meetsAAContrast(foreground: Int, background: Int): Boolean {
        return calculateContrast(foreground, background) >= 4.5
    }

    /**
     * Checks if two colors satisfy a specific minimum contrast ratio.
     */
    fun meetsContrast(foreground: Int, background: Int, minRatio: Double = 3.0): Boolean {
        return calculateContrast(foreground, background) >= minRatio
    }

    /**
     * Checks whether assigning [swatchColor] to [roleCode] satisfies all template constraints.
     */
    fun isSwatchValidForRole(
        swatchColor: Int,
        roleCode: String,
        currentAssignments: Map<String, Int>,
        constraints: List<watch.facesapp.core.model.ContrastConstraint>
    ): Boolean {
        for (constraint in constraints) {
            if (constraint.foregroundRole.equals(roleCode, ignoreCase = true)) {
                val bg = currentAssignments[constraint.backgroundRole.uppercase()]
                if (bg != null && calculateContrast(swatchColor, bg) < constraint.minRatio) {
                    return false
                }
            } else if (constraint.backgroundRole.equals(roleCode, ignoreCase = true)) {
                val fg = currentAssignments[constraint.foregroundRole.uppercase()]
                if (fg != null && calculateContrast(fg, swatchColor) < constraint.minRatio) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Evaluates whether the full set of role color assignments satisfies all template constraints.
     */
    fun satisfiesAllConstraints(
        assignments: Map<String, Int>,
        constraints: List<watch.facesapp.core.model.ContrastConstraint>
    ): Boolean {
        for (constraint in constraints) {
            val fg = assignments[constraint.foregroundRole.uppercase()] ?: continue
            val bg = assignments[constraint.backgroundRole.uppercase()] ?: continue
            if (calculateContrast(fg, bg) < constraint.minRatio) {
                return false
            }
        }
        return true
    }

    /**
     * Calculates the cumulative contrast score for a set of role color assignments.
     */
    fun calculateCumulativeScore(
        assignments: Map<String, Int>,
        constraints: List<watch.facesapp.core.model.ContrastConstraint>
    ): Double {
        var total = 0.0
        for (constraint in constraints) {
            val fg = assignments[constraint.foregroundRole.uppercase()] ?: continue
            val bg = assignments[constraint.backgroundRole.uppercase()] ?: continue
            total += calculateContrast(fg, bg)
        }
        return total
    }
}
