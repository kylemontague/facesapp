package watch.facesapp.mobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import watch.facesapp.core.R

val QuicksandFontFamily = FontFamily(
    Font(R.font.quicksand_bold, FontWeight.Bold)
)

val ProximaNovaFontFamily = FontFamily(
    Font(R.font.proximanova_light, FontWeight.Light),
    Font(R.font.proximanova_semibold, FontWeight.SemiBold)
)

val FacesTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = QuicksandFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        color = PureWhite
    ),
    headlineMedium = TextStyle(
        fontFamily = QuicksandFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = PureWhite
    ),
    titleLarge = TextStyle(
        fontFamily = QuicksandFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = PureWhite
    ),
    bodyLarge = TextStyle(
        fontFamily = ProximaNovaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = PureWhite
    ),
    bodyMedium = TextStyle(
        fontFamily = ProximaNovaFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        color = SubtitleGray
    ),
    labelLarge = TextStyle(
        fontFamily = QuicksandFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 1.2.sp,
        color = PureWhite
    ),
    labelMedium = TextStyle(
        fontFamily = QuicksandFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.0.sp,
        color = PureWhite
    )
)
