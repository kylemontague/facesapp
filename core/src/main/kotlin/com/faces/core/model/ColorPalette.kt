package com.faces.core.model

data class ColorPalette(
    val colors: List<Int> = defaultPalette()
) {
    init {
        require(colors.isNotEmpty()) { "ColorPalette must contain at least one color" }
    }

    fun getColor(index: Int, fallback: Int = 0xFFFFFFFF.toInt()): Int {
        return if (index in colors.indices) colors[index] else fallback
    }

    companion object {
        fun defaultPalette(): List<Int> = listOf(
            0xFF080A0D.toInt(), // 0: Deep luxury black
            0xFFFFFFFF.toInt(), // 1: Crisp white
            0xFF89C4BB.toInt(), // 2: Mint / teal
            0xFFCDE9E6.toInt(), // 3: Soft ice
            0xFF131947.toInt(), // 4: Midnight navy
            0xFFC7FD00.toInt(), // 5: Neon lime
            0xFF55EE45.toInt(), // 6: Vibrant green
            0xFF3C797F.toInt(), // 7: Deep cyan
            0xFF2369AD.toInt(), // 8: Prism blue 1
            0xFF32A3C2.toInt(), // 9: Prism blue 2
            0xFF4E8EB4.toInt(), // 10: Prism blue 3
            0xFFD93550.toInt(), // 11: Crimson red
            0xFF34A5A4.toInt(), // 12: Accent turquoise
            0xFF662D91.toInt(), // 13: Purple
            0xFFE6E6E6.toInt(), // 14: Light gray
            0xFF161B21.toInt()  // 15: Dark charcoal
        )
    }
}
