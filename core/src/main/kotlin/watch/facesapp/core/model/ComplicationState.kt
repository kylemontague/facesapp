package watch.facesapp.core.model

/**
 * Live health, battery, and activity metrics passed to watch face renderers
 * when displaying complications in the tap cycle.
 *
 * If a complication metric is null, it indicates that no live reading has been
 * received yet from hardware sensors or sync services.
 */
data class ComplicationState(
    val batteryPercent: Int? = null,
    val stepCount: Long? = null,
    val heartRateBpm: Int? = null,
    val caloriesBurned: Int? = null,
    val distanceKm: Float? = null,
    val weatherTempC: Int? = null,
    val weatherCondition: String? = null,
    val nextEventTitle: String? = null,
    val nextEventMinutes: Int? = null,
    val sunriseSunsetSummary: String? = null,
    val sunriseSunsetIsSunset: Boolean = true,
    val activeMinutes: Int? = null
) {
    /**
     * Returns true only if the complication mode has received at least one genuine live reading.
     * TIME and DATE are always backed by the local system clock and are always available.
     */
    fun hasLiveValue(mode: FaceMode): Boolean = when (mode) {
        FaceMode.TIME -> true
        FaceMode.DATE -> true
        FaceMode.BATTERY -> batteryPercent != null && batteryPercent in 0..100
        FaceMode.STEPS -> stepCount != null
        FaceMode.HEART_RATE -> heartRateBpm != null && heartRateBpm > 0
        FaceMode.CALORIES -> caloriesBurned != null
        FaceMode.DISTANCE -> distanceKm != null
        FaceMode.WEATHER -> weatherTempC != null && !weatherCondition.isNullOrBlank()
        FaceMode.NEXT_EVENT -> !nextEventTitle.isNullOrBlank() && nextEventMinutes != null
        FaceMode.SUNRISE_SUNSET -> !sunriseSunsetSummary.isNullOrBlank()
        FaceMode.ACTIVE_MINUTES -> activeMinutes != null
    }

    companion object {
        /**
         * Sample populated state for mobile design previews and tests.
         */
        val PREVIEW_SAMPLE = ComplicationState(
            batteryPercent = 85,
            stepCount = 7420L,
            heartRateBpm = 72,
            caloriesBurned = 420,
            distanceKm = 5.2f,
            weatherTempC = 21,
            weatherCondition = "Sunny",
            nextEventTitle = "Design Review",
            nextEventMinutes = 25,
            sunriseSunsetSummary = "Sunset 18:42",
            sunriseSunsetIsSunset = true,
            activeMinutes = 45
        )
    }
}
