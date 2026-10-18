package watch.facesapp.mobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import watch.facesapp.mobile.ui.theme.BorderWhite
import watch.facesapp.mobile.ui.theme.FrostedWhite
import watch.facesapp.mobile.ui.theme.PureWhite
import watch.facesapp.mobile.ui.theme.QuicksandFontFamily

@Composable
fun FacesPillButton(
    text: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = FrostedWhite,
    contentColor: Color = PureWhite,
    borderColor: Color = BorderWhite
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.15f),
            disabledContentColor = contentColor.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, borderColor.copy(alpha = if (enabled) 1f else 0.4f)),
        contentPadding = if (text.isNotBlank()) {
            PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        } else {
            PaddingValues(0.dp)
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text.ifBlank { "Action" },
                    tint = PureWhite
                )
            }
            if (icon != null && text.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
            }
            if (text.isNotBlank()) {
                Text(
                    text = text.uppercase(),
                    fontFamily = QuicksandFontFamily,
                    fontSize = 15.sp,
                    letterSpacing = 1.2.sp,
                    color = PureWhite
                )
            }
        }
    }
}
