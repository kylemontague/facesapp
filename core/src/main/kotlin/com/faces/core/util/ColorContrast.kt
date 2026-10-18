package com.faces.core.util

import android.graphics.Color
import androidx.core.graphics.ColorUtils

object ColorContrast {

    /**
     * Computes the contrast ratio between two ARGB colors.
     * Returns a value between 1.0 and 21.0.
     */
    fun calculateContrast(foreground: Int, background: Int): Double {
        return ColorUtils.calculateContrast(foreground, background)
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
}
