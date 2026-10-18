package com.faces.mobile.camera

import android.graphics.Bitmap
import android.graphics.Color
import androidx.palette.graphics.Palette
import com.faces.core.model.ColorPalette

object PaletteExtractor {

    /**
     * Extracts a curated 16-color palette from a Bitmap sorted by saturation and vibrancy.
     */
    fun extract16Colors(bitmap: Bitmap): List<Int> {
        // Downsample bitmap for fast palette computation
        val scaled = Bitmap.createScaledBitmap(bitmap, 200, 200, false)
        val palette = Palette.from(scaled)
            .maximumColorCount(24)
            .generate()

        val extractedColors = mutableSetOf<Int>()

        // 1. Add key prominent swatches first
        palette.vibrantSwatch?.rgb?.let { extractedColors.add(it) }
        palette.lightVibrantSwatch?.rgb?.let { extractedColors.add(it) }
        palette.darkVibrantSwatch?.rgb?.let { extractedColors.add(it) }
        palette.mutedSwatch?.rgb?.let { extractedColors.add(it) }
        palette.lightMutedSwatch?.rgb?.let { extractedColors.add(it) }
        palette.darkMutedSwatch?.rgb?.let { extractedColors.add(it) }

        // 2. Add remaining swatches sorted by saturation
        val remainingSwatches = palette.swatches
            .map { it.rgb }
            .filter { it !in extractedColors }
            .sortedByDescending { color ->
                val hsv = FloatArray(3)
                Color.colorToHSV(color, hsv)
                hsv[1] * hsv[2] // saturation * brightness
            }

        for (c in remainingSwatches) {
            extractedColors.add(c)
            if (extractedColors.size >= 16) break
        }

        // 3. If image lacks color variety, pad with rich default palette swatches
        if (extractedColors.size < 16) {
            for (defaultColor in ColorPalette.defaultPalette()) {
                extractedColors.add(defaultColor)
                if (extractedColors.size >= 16) break
            }
        }

        return extractedColors.take(16)
    }
}
