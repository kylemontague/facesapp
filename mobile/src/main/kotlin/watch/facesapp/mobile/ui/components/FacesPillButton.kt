package watch.facesapp.mobile.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import watch.facesapp.mobile.ui.theme.BorderWhite
import watch.facesapp.mobile.ui.theme.FrostedWhite
import watch.facesapp.mobile.ui.theme.PureWhite
import watch.facesapp.mobile.ui.theme.QuicksandFontFamily

@Composable
fun FacesPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FrostedWhite,
            contentColor = PureWhite,
            disabledContainerColor = FrostedWhite.copy(alpha = 0.15f),
            disabledContentColor = PureWhite.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, BorderWhite.copy(alpha = if (enabled) 1f else 0.4f)),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PureWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
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
