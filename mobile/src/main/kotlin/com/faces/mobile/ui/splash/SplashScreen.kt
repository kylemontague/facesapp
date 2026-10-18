package com.faces.mobile.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import slurpp.faces.R
import com.faces.mobile.ui.components.BottomVignette
import com.faces.mobile.ui.components.TopVignette
import com.faces.mobile.ui.theme.AccentGreen
import com.faces.mobile.ui.theme.LuxuryBlack
import com.faces.mobile.ui.theme.QuicksandFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Modern Compose reproduction of the artisanal 2017 Faces splash screen.
 * Recreates the iconic morphing watch loader animation, luxury black backdrop,
 * signature top/bottom vignettes, and Quicksand 'MADE BY SLURPP' branding.
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val frames = remember {
        listOf(
            R.drawable.faces_face_dark,
            R.drawable.faces_face_dark_square,
            R.drawable.faces_face_light_square,
            R.drawable.faces_face_light
        )
    }

    var currentFrameIndex by remember { mutableIntStateOf(0) }

    // Morphing watch face loader animation: 300ms per frame
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(300L)
            currentFrameIndex = (currentFrameIndex + 1) % frames.size
        }
    }

    // Auto-transition to home after 1.8s (allowing 1.5 full animation cycles)
    LaunchedEffect(Unit) {
        delay(1800L)
        onSplashComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBlack)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Allow tapping to skip immediately
                onSplashComplete()
            },
        contentAlignment = Alignment.Center
    ) {
        // Centered watch strap with morphing loader face
        Image(
            painter = painterResource(id = frames[currentFrameIndex]),
            contentDescription = "Faces loader animation",
            modifier = Modifier
                .width(284.dp)
                .fillMaxHeight(),
            contentScale = ContentScale.Fit
        )

        // Vignette overlays
        TopVignette(modifier = Modifier.align(Alignment.TopCenter))
        BottomVignette(modifier = Modifier.align(Alignment.BottomCenter))

        // Artisanal bottom branding: MADE BY SLURPP
        Text(
            text = stringResource(id = R.string.madeby).uppercase(),
            fontFamily = QuicksandFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            letterSpacing = 2.sp,
            color = AccentGreen,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        )
    }
}
