package watch.facesapp.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import watch.facesapp.mobile.ui.theme.PureBlack

@Composable
fun TopVignette(
    modifier: Modifier = Modifier,
    height: Dp = 120.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PureBlack.copy(alpha = 0.85f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun BottomVignette(
    modifier: Modifier = Modifier,
    height: Dp = 140.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        PureBlack.copy(alpha = 0.90f)
                    )
                )
            )
    )
}
