package watch.facesapp.mobile.ui.customise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import watch.facesapp.core.model.ContrastGuardMode
import watch.facesapp.mobile.sensor.ShakeDetector
import watch.facesapp.mobile.ui.components.BottomVignette
import watch.facesapp.mobile.ui.components.FacesPillButton
import watch.facesapp.mobile.ui.components.SwatchGrid
import watch.facesapp.mobile.ui.components.SwatchMarker
import watch.facesapp.mobile.ui.components.TopInfoPill
import watch.facesapp.mobile.ui.components.TopVignette
import watch.facesapp.mobile.ui.components.WatchFacePreview
import watch.facesapp.mobile.ui.theme.LuxuryBlack
import watch.facesapp.mobile.ui.theme.PureWhite
import watch.facesapp.mobile.ui.theme.QuicksandFontFamily
import watch.facesapp.mobile.ui.theme.SubtitleGray
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
    val activeComplications by viewModel.activeComplications.collectAsState()
    val previewFaceMode by viewModel.previewFaceMode.collectAsState()
    val contrastGuardMode by viewModel.contrastGuardMode.collectAsState()
    val swatchContrastStatuses by viewModel.swatchContrastStatuses.collectAsState()

    // Start real-time preview session on watch and keep watch screen awake while customizing
    DisposableEffect(Unit) {
        viewModel.startPreviewSession()
        onDispose {
            // Note: Preview session lifecycle is maintained while MainActivity is active
        }
    }

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
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PureWhite
                        )
                    }
                }

                TopInfoPill(
                    text = style.displayName
                )

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isGuardOn = contrastGuardMode == ContrastGuardMode.HARD
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleContrastGuardMode()
                    }) {
                        Icon(
                            painter = painterResource(
                                id = if (isGuardOn) watch.facesapp.core.R.drawable.contrast_24 else watch.facesapp.core.R.drawable.contrast_rtl_off_24
                            ),
                            contentDescription = if (isGuardOn) "Contrast Guard Enabled" else "Contrast Guard Disabled",
                            tint = if (isGuardOn) PureWhite else PureWhite.copy(alpha = 0.5f)
                        )
                    }

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
            }

            Spacer(modifier = Modifier.height(16.dp))

            val previewCalendar = remember { Calendar.getInstance() }
            WatchFacePreview(
                config = config,
                calendar = previewCalendar,
                isAmbient = false,
                faceMode = previewFaceMode,
                bezelSize = 180.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.cyclePreviewMode()
                    }
            )

            Text(
                text = "TAP PREVIEW TO CYCLE • ${previewFaceMode.displayName.uppercase()}",
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = SubtitleGray,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // 3. 4x4 Swatch Grid with drag-to-reassign, contrast filtering and snap-to-grid
            SwatchGrid(
                paletteColors = paletteColors,
                markers = markers,
                selectedRole = selectedRole,
                swatchStatuses = swatchContrastStatuses,
                onRoleSelected = { role -> viewModel.selectRole(role) },
                onRoleAssigned = { role, index -> viewModel.assignRoleToSwatch(role, index) },
                onSwatchClick = { index ->
                    viewModel.assignCurrentRoleToSwatch(index)
                },
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Action: Save Face Button
            FacesPillButton(
                text = "Save Face",
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
