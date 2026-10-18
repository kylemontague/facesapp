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
    CALORIES("calories", "Calories", "kcal");

    companion object {
        fun fromId(id: String): FaceMode? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }

        val DEFAULT_CYCLE = listOf(TIME, DATE)
        val ALL_COMPLICATIONS = listOf(TIME, DATE, BATTERY, STEPS, HEART_RATE, CALORIES)
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
}

enum class WatchShape {
    ROUND,
    SQUARE
}
