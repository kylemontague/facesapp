package watch.facesapp.core.model

/**
 * Defines a legibility and contrast relationship between two layered or adjacent color roles
 * within a watch face template (e.g. Foreground text/numerals on Background dial).
 */
data class ContrastConstraint(
    val foregroundRole: String,       // e.g. "TXT", "HR", "MIN", "SEC"
    val backgroundRole: String,       // e.g. "BG"
    val minRatio: Double = 3.0,       // Minimum WCAG contrast ratio (3.0 for large/display, 4.5 for text)
    val description: String = ""
)

/**
 * Mode of enforcement for color contrast rules in the customizer.
 */
enum class ContrastGuardMode {
    HARD,  // Strictly disables swatches that fail contrast constraints
    SOFT   // Allows selection, but displays a subtle warning badge on low-contrast swatches
}

/**
 * Visual and interactive state of an individual swatch in the 4x4 grid.
 */
enum class SwatchContrastStatus {
    VALID,     // Meets or exceeds required contrast ratio
    WARNING,   // Fails contrast ratio in SOFT mode (advisory warning badge)
    DISABLED   // Fails contrast ratio in HARD mode (dimmed, unselectable)
}
