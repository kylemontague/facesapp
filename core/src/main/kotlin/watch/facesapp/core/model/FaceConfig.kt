package watch.facesapp.core.model

import java.util.UUID

data class FaceConfig(
    val id: String = UUID.randomUUID().toString(),
    val style: FaceStyle = FaceStyle.OUA_TIME,
    val colors: List<Int> = defaultColorsForStyle(style),
    val options: Map<String, String> = emptyMap(),
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getBackgroundColor(isAmbient: Boolean): Int {
        if (isAmbient) return 0xFF000000.toInt()
        return colors.getOrElse(0) { defaultColorsForStyle(style)[0] }
    }

    fun getHourColor(isAmbient: Boolean): Int {
        if (isAmbient) return 0xFFFFFFFF.toInt()
        return colors.getOrElse(1) { defaultColorsForStyle(style).getOrElse(1) { 0xFFFFFFFF.toInt() } }
    }

    fun getMinuteColor(isAmbient: Boolean): Int {
        if (isAmbient) return 0xFFFFFFFF.toInt()
        return colors.getOrElse(2) { defaultColorsForStyle(style).getOrElse(2) { 0xFFFFFFFF.toInt() } }
    }

    fun getSecondColor(isAmbient: Boolean): Int {
        if (isAmbient) return 0x00000000 // In ambient mode second hand is hidden
        return colors.getOrElse(3) { defaultColorsForStyle(style).getOrElse(3) { 0xFFFFFFFF.toInt() } }
    }

    fun getAccentColor(isAmbient: Boolean): Int {
        if (isAmbient) return 0xFFFFFFFF.toInt()
        return colors.getOrElse(4) { defaultColorsForStyle(style).getOrElse(4) { 0xFFFFFFFF.toInt() } }
    }

    fun getMeridianColor(isAmbient: Boolean): Int {
        if (isAmbient) return 0xFFFFFFFF.toInt()
        return colors.getOrElse(5) { defaultColorsForStyle(style).getOrElse(5) { 0xFFFFFFFF.toInt() } }
    }

    /**
     * Active complication tap cycle (defaults to [FaceMode.TIME, FaceMode.DATE]).
     */
    val activeComplications: List<FaceMode>
        get() {
            val raw = options["complications"]
            if (raw.isNullOrBlank()) {
                return FaceMode.DEFAULT_CYCLE
            }
            val parsed = raw.split(",").mapNotNull { FaceMode.fromId(it.trim()) }
            if (parsed.isEmpty()) {
                return FaceMode.DEFAULT_CYCLE
            }
            return if (FaceMode.TIME in parsed) {
                listOf(FaceMode.TIME) + parsed.filter { it != FaceMode.TIME }
            } else {
                listOf(FaceMode.TIME) + parsed
            }
        }

    fun withComplications(complications: List<FaceMode>): FaceConfig {
        val serialized = complications.joinToString(",") { it.id }
        return copy(options = options + ("complications" to serialized))
    }

    companion object {
        fun defaultColorsForStyle(style: FaceStyle): List<Int> = style.template.defaultColors
    }
}
