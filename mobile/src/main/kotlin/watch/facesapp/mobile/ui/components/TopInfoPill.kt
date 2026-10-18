package watch.facesapp.mobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import watch.facesapp.mobile.ui.theme.FrostedPill
import watch.facesapp.mobile.ui.theme.PureWhite
import watch.facesapp.mobile.ui.theme.QuicksandFontFamily

@Composable
fun TopInfoPill(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(FrostedPill, shape = RoundedCornerShape(50))
            .border(
                BorderStroke(0.5.dp, Color(0x44FFFFFF)),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = QuicksandFontFamily,
            fontSize = 13.sp,
            letterSpacing = 0.8.sp,
            color = PureWhite
        )
    }
}
