package com.faces.mobile.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.faces.mobile.ui.components.BottomVignette
import com.faces.mobile.ui.components.FacesPillButton
import com.faces.mobile.ui.components.TopInfoPill
import com.faces.mobile.ui.components.TopVignette
import com.faces.mobile.ui.components.WatchFacePreview
import com.faces.mobile.ui.theme.LuxuryBlack
import com.faces.mobile.ui.theme.PureWhite

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToCustomise: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val faces by viewModel.faces.collectAsState()
    val isAmbient by viewModel.isAmbient.collectAsState()
    val calendar by viewModel.calendar.collectAsState()
    val faceMode by viewModel.faceMode.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { faces.size.coerceAtLeast(1) }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBlack)
    ) {
        // 1. Horizontal Pager with depth transformation
        if (faces.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val faceEntity = faces.getOrNull(page)
                if (faceEntity != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                if (pageOffset < -1f || pageOffset > 1f) {
                                    alpha = 0f
                                } else if (pageOffset <= 0f) {
                                    alpha = 1f
                                    scaleX = 1f
                                    scaleY = 1f
                                    translationX = 0f
                                } else {
                                    alpha = 1f - pageOffset
                                    val scale = 0.75f + (1f - 0.75f) * (1f - pageOffset)
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = size.width * -pageOffset
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        WatchFacePreview(
                            config = faceEntity.toFaceConfig(),
                            calendar = calendar,
                            isAmbient = isAmbient,
                            faceMode = faceMode,
                            modifier = Modifier
                                .fillMaxSize(0.85f)
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {
                                    viewModel.toggleFaceMode()
                                }
                        )
                    }
                }
            }
        }

        // 2. Top Vignette Scrim & Status Controls
        TopVignette(modifier = Modifier.align(Alignment.TopCenter))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 40.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ambient / Screen On toggle
            IconButton(onClick = { viewModel.toggleAmbientMode() }) {
                Icon(
                    imageVector = if (isAmbient) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Toggle Ambient Mode",
                    tint = PureWhite
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            val currentFace = faces.getOrNull(pagerState.currentPage)
            TopInfoPill(text = currentFace?.name ?: "Faces")

            Spacer(modifier = Modifier.weight(1f))

            // Favorite button
            IconButton(
                onClick = { currentFace?.let { viewModel.toggleFavorite(it) } }
            ) {
                Icon(
                    imageVector = if (currentFace?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite Face",
                    tint = if (currentFace?.isFavorite == true) Color(0xFFD93550) else PureWhite
                )
            }
        }

        // 3. Bottom Vignette Scrim & Action Buttons
        BottomVignette(modifier = Modifier.align(Alignment.BottomCenter))

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentFace = faces.getOrNull(pagerState.currentPage)

            FacesPillButton(
                text = "Customise",
                onClick = {
                    currentFace?.let {
                        onNavigateToCustomise(it.id)
                    }
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            FacesPillButton(
                text = "Snap Photo",
                icon = Icons.Default.CameraAlt,
                onClick = onNavigateToCamera,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
