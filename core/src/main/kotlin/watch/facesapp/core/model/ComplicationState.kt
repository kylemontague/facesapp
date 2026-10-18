package watch.facesapp.core.model

/**
 * Live health, battery, and activity metrics passed to watch face renderers
 * when displaying complications in the tap cycle.
 */
data class ComplicationState(
    val batteryPercent: Int = 85,
    val stepCount: Long = 7420L,
    val heartRateBpm: Int = 72,
    val caloriesBurned: Int = 420
)
