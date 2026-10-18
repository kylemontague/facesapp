package watch.facesapp.core.model

enum class FaceMode(
    val id: String,
    val displayName: String,
    val defaultUnit: String
) {
    TIME("time", "Time", ""),
    DATE("date", "Date", ""),
    BATTERY("battery", "Battery", "%"),
    STEPS("steps", "Steps", "steps"),
    HEART_RATE("heart_rate", "Heart Rate", "bpm"),
    CALORIES("calories", "Calories", "kcal"),
    DISTANCE("distance", "Distance", "km"),
    WEATHER("weather", "Weather", "°C"),
    NEXT_EVENT("next_event", "Next Event", ""),
    SUNRISE_SUNSET("sunrise_sunset", "Sun Cycle", ""),
    ACTIVE_MINUTES("active_minutes", "Active Time", "mins");

    companion object {
        fun fromId(id: String): FaceMode? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }

        val DEFAULT_CYCLE = listOf(TIME, DATE)
        val ALL_COMPLICATIONS = listOf(
            TIME,
            DATE,
            BATTERY,
            STEPS,
            HEART_RATE,
            CALORIES,
            DISTANCE,
            WEATHER,
            NEXT_EVENT,
            SUNRISE_SUNSET,
            ACTIVE_MINUTES
        )
    }

    /**
     * Advances to the next active complication in [activeCycle].
     * If the current mode is not in the cycle or is at the end, wraps to the first item.
     */
    fun next(activeCycle: List<FaceMode>): FaceMode {
        if (activeCycle.isEmpty()) return TIME
        val idx = activeCycle.indexOf(this)
        return if (idx == -1 || idx >= activeCycle.lastIndex) {
            activeCycle.first()
        } else {
            activeCycle[idx + 1]
        }
    }

    /**
     * Advances to the next active complication in [activeCycle] that has a confirmed live value.
     * Any complication that has never been initialized or has no live data is automatically skipped.
     */
    fun nextAvailable(activeCycle: List<FaceMode>, state: ComplicationState): FaceMode {
        val available = activeCycle.filter { state.hasLiveValue(it) }
        if (available.isEmpty()) return TIME
        val idx = available.indexOf(this)
        return if (idx == -1 || idx >= available.lastIndex) {
            available.first()
        } else {
            available[idx + 1]
        }
    }
}

enum class WatchShape {
    ROUND,
    SQUARE
}
