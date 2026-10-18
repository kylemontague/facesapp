package com.faces.mobile.ui.customise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faces.core.model.FaceStyle
import com.faces.mobile.sensor.ShakeDetector
import com.faces.mobile.ui.components.BottomVignette
import com.faces.mobile.ui.components.FacesPillButton
import com.faces.mobile.ui.components.SwatchGrid
import com.faces.mobile.ui.components.SwatchMarker
import com.faces.mobile.ui.components.TopInfoPill
import com.faces.mobile.ui.components.TopVignette
import com.faces.mobile.ui.components.WatchFacePreview
import com.faces.mobile.ui.theme.BorderWhite
import com.faces.mobile.ui.theme.FrostedPill
import com.faces.mobile.ui.theme.FrostedWhite
import com.faces.mobile.ui.theme.LuxuryBlack
import com.faces.mobile.ui.theme.PureWhite
import com.faces.mobile.ui.theme.QuicksandFontFamily
import java.util.Calendar

@Composable
fun CustomiseScreen(
    viewModel: CustomiseViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val style by viewModel.style.collectAsState()
    val paletteColors by viewModel.paletteColors.collectAsState()
    val roleAssignments by viewModel.roleAssignments.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val config by viewModel.currentConfig.collectAsState()

    val shakeDetector = remember(context) { ShakeDetector(context) }

    // Shake gesture listener
    LaunchedEffect(shakeDetector) {
        shakeDetector.shakeEvents().collect {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.shuffleAssignments()
        }
    }

    val markers = remember(roleAssignments, style) {
        val required = style.requiredRoles()
        roleAssignments
            .filter { (role, _) -> role in required }
            .map { (role, index) ->
                SwatchMarker(role = role, colorIndex = index)
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar with fixed template title pill
            Spacer(modifier = Modifier.height(36.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = PureWhite
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TopInfoPill(
                    text = style.displayName
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.shuffleAssignments()
                }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle Palette",
                        tint = PureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Live Watchface Preview
            WatchFacePreview(
                config = config,
                calendar = Calendar.getInstance(),
                isAmbient = false,
                bezelSize = 180.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. 4x4 Swatch Grid with drag-to-reassign and snap-to-grid
            SwatchGrid(
                paletteColors = paletteColors,
                markers = markers,
                selectedRole = selectedRole,
                onRoleSelected = { role -> viewModel.selectRole(role) },
                onRoleAssigned = { role, index -> viewModel.assignRoleToSwatch(role, index) },
                onSwatchClick = { index ->
                    viewModel.assignCurrentRoleToSwatch(index)
                },
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Save Button
            FacesPillButton(
                text = "Save Watch Face",
                onClick = {
                    viewModel.saveFace(onSaved = onNavigateBack)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }

        TopVignette(modifier = Modifier.align(Alignment.TopCenter), height = 40.dp)
        BottomVignette(modifier = Modifier.align(Alignment.BottomCenter), height = 50.dp)
    }
}
