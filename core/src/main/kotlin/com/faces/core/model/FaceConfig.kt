package com.faces.core.model

import java.util.UUID

data class FaceConfig(
    val id: String = UUID.randomUUID().toString(),
    val style: FaceStyle = FaceStyle.OUA_TIME,
    val colors: List<Int> = defaultColorsForStyle(style),
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

    companion object {
        fun defaultColorsForStyle(style: FaceStyle): List<Int> = when (style) {
            FaceStyle.OUA_TIME -> listOf(
                0xFF080A0D.toInt(), // Background
                0xFFFFFFFF.toInt(), // Hour & words
                0xFFFFFFFF.toInt()  // Second arc
            )
            FaceStyle.FILL_TIME -> listOf(
                0xFF89C4BB.toInt(), // Background
                0xFFCDE9E6.toInt(), // Accent (fill split)
                0xFFFFFFFF.toInt()  // Text
            )
            FaceStyle.BUSINESS_TIME -> listOf(
                0xFF131947.toInt(), // Background
                0xFFC7FD00.toInt(), // Hour hand
                0xFF55EE45.toInt(), // Minute hand
                0xFF3C797F.toInt()  // Second hand
            )
            FaceStyle.TWO_TIME -> listOf(
                0xFF080A0D.toInt(), // Background
                0xFFFFFFFF.toInt(), // Accent (split gradient)
                0xFFFFFFFF.toInt()  // Digits & dividing line
            )
            FaceStyle.PRISM_TIME -> listOf(
                0xFF2369AD.toInt(), // Mesh color 1
                0xFF32A3C2.toInt(), // Mesh color 2
                0xFF4E8EB4.toInt(), // Mesh color 3
                0xFFFFFFFF.toInt()  // Text
            )
        }
    }
}
