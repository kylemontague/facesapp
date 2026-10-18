package com.faces.mobile.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faces.core.util.ColorContrast
import com.faces.mobile.ui.theme.BorderWhite
import com.faces.mobile.ui.theme.QuicksandFontFamily

data class SwatchMarker(
    val role: String, // "BG", "HR", "MIN", "SEC", "ACC"
    val colorIndex: Int
)

@Composable
fun SwatchGrid(
    paletteColors: List<Int>,
    markers: List<SwatchMarker>,
    onSwatchClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        itemsIndexed(paletteColors.take(16)) { index, colorInt ->
            val color = Color(colorInt)
            val matchedMarkers = markers.filter { it.colorIndex == index }
            val isSelected = matchedMarkers.isNotEmpty()

            val borderWidth by animateDpAsState(
                targetValue = if (isSelected) 3.dp else 0.dp,
                label = "borderWidth"
            )

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) {
                            Modifier.border(BorderStroke(borderWidth, BorderWhite), CircleShape)
                        } else {
                            Modifier.border(BorderStroke(0.5.dp, Color(0x33FFFFFF)), CircleShape)
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onSwatchClick(index)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (matchedMarkers.isNotEmpty()) {
                    val label = matchedMarkers.joinToString("/") { it.role }
                    val textColor = if (ColorContrast.getContrastingTextColor(colorInt) == 0xFFFFFFFF.toInt()) {
                        Color.White
                    } else {
                        Color.Black
                    }

                    Text(
                        text = label,
                        fontFamily = QuicksandFontFamily,
                        fontSize = 11.sp,
                        color = textColor
                    )
                }
            }
        }
    }
}
