package watch.facesapp.mobile.ui.customise

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import watch.facesapp.core.model.FaceMode
import watch.facesapp.mobile.ui.components.FacesPillButton
import watch.facesapp.mobile.ui.theme.BorderWhite
import watch.facesapp.mobile.ui.theme.FrostedWhite
import watch.facesapp.mobile.ui.theme.LuxuryBlack
import watch.facesapp.mobile.ui.theme.ProximaNovaFontFamily
import watch.facesapp.mobile.ui.theme.PureWhite
import watch.facesapp.mobile.ui.theme.QuicksandFontFamily
import watch.facesapp.mobile.ui.theme.SubtitleGray

private fun FaceMode.icon(): ImageVector = when (this) {
    FaceMode.TIME -> Icons.Default.AccessTime
    FaceMode.DATE -> Icons.Default.CalendarToday
    FaceMode.BATTERY -> Icons.Default.BatteryChargingFull
    FaceMode.STEPS -> Icons.AutoMirrored.Filled.DirectionsWalk
    FaceMode.HEART_RATE -> Icons.Default.Favorite
    FaceMode.CALORIES -> Icons.Default.LocalFireDepartment
}

private fun FaceMode.description(): String = when (this) {
    FaceMode.TIME -> "Primary watch face time display"
    FaceMode.DATE -> "Current calendar date and month"
    FaceMode.BATTERY -> "Real-time battery percentage"
    FaceMode.STEPS -> "Daily step count tracker"
    FaceMode.HEART_RATE -> "Heart rate in beats per minute"
    FaceMode.CALORIES -> "Estimated active energy burned"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplicationsSheet(
    activeComplications: List<FaceMode>,
    onToggleComplication: (FaceMode) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LuxuryBlack,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TAP COMPLICATIONS",
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 1.2.sp,
                        color = PureWhite
                    )
                    Text(
                        text = "Customize the data sequence cycled when tapping the dial",
                        fontFamily = ProximaNovaFontFamily,
                        fontSize = 13.sp,
                        color = SubtitleGray
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = PureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Cycle Pill Sequence Preview
            Text(
                text = "ACTIVE CYCLE SEQUENCE",
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = SubtitleGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FrostedWhite)
                    .border(BorderStroke(1.dp, BorderWhite), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                activeComplications.forEachIndexed { index, mode ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x33FFFFFF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = mode.displayName.uppercase(),
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = PureWhite
                        )
                    }

                    if (index < activeComplications.lastIndex) {
                        Text(
                            text = "→",
                            fontFamily = ProximaNovaFontFamily,
                            fontSize = 12.sp,
                            color = SubtitleGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // List of all complications with toggles and reordering
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(FaceMode.ALL_COMPLICATIONS) { _, mode ->
                    val isActive = mode in activeComplications
                    val activeIndex = activeComplications.indexOf(mode)
                    val isLocked = mode == FaceMode.TIME

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isActive) FrostedWhite else Color(0x11FFFFFF))
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isActive) BorderWhite else Color(0x22FFFFFF)
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isActive) Color(0x33FFFFFF) else Color(0x11FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = mode.icon(),
                                contentDescription = mode.displayName,
                                tint = if (isActive) PureWhite else SubtitleGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Title & Subtitle
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = mode.displayName,
                                    fontFamily = QuicksandFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isActive) PureWhite else SubtitleGray
                                )
                                if (isLocked) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Required",
                                        tint = SubtitleGray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Text(
                                text = mode.description(),
                                fontFamily = ProximaNovaFontFamily,
                                fontSize = 12.sp,
                                color = SubtitleGray
                            )
                        }

                        // Reorder buttons (only if active and not locked)
                        if (isActive && !isLocked) {
                            IconButton(
                                onClick = { onMoveUp(activeIndex) },
                                enabled = activeIndex > 1, // index 0 is Time
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Move Up",
                                    tint = if (activeIndex > 1) PureWhite else Color(0x44FFFFFF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { onMoveDown(activeIndex) },
                                enabled = activeIndex < activeComplications.lastIndex,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Move Down",
                                    tint = if (activeIndex < activeComplications.lastIndex) PureWhite else Color(0x44FFFFFF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Toggle Switch (disabled for Time)
                        Switch(
                            checked = isActive,
                            onCheckedChange = { if (!isLocked) onToggleComplication(mode) },
                            enabled = !isLocked,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = Color(0x66FFFFFF),
                                uncheckedThumbColor = SubtitleGray,
                                uncheckedTrackColor = Color(0x22FFFFFF),
                                disabledCheckedThumbColor = PureWhite.copy(alpha = 0.5f),
                                disabledCheckedTrackColor = Color(0x33FFFFFF)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Done Button
            FacesPillButton(
                text = "Apply Complications",
                icon = Icons.Default.Check,
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
