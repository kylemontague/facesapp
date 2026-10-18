package watch.facesapp.mobile.ui.customise

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
    onReorderComplications: (List<FaceMode>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val currentComplications by rememberUpdatedState(activeComplications)
    val onReorderUpdated by rememberUpdatedState(onReorderComplications)

    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var itemHeightPx by remember { mutableFloatStateOf(0f) }
    var lastHapticIndex by remember { mutableStateOf<Int?>(null) }

    val inactiveComplications = remember(activeComplications) {
        FaceMode.ALL_COMPLICATIONS.filter { it !in activeComplications }
    }

    val dIdx = draggingIndex
    val targetIndex = if (dIdx != null && itemHeightPx > 0f) {
        val displacement = (dragOffsetY / itemHeightPx).roundToInt()
        (dIdx + displacement).coerceIn(1, activeComplications.lastIndex)
    } else {
        dIdx ?: 1
    }

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
                .verticalScroll(rememberScrollState(), enabled = draggingIndex == null)
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
                        text = "Drag to reorder the tap cycle sequence",
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

            Spacer(modifier = Modifier.height(18.dp))

            // 1. ACTIVE CYCLE (Reorderable via Drag)
            Text(
                text = "ACTIVE CYCLE (DRAG TO REORDER)",
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = SubtitleGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                activeComplications.forEachIndexed { index, mode ->
                    val isBeingDragged = draggingIndex == index
                    val isLocked = mode == FaceMode.TIME

                    val scale by animateFloatAsState(
                        targetValue = if (isBeingDragged) 1.04f else 1.0f,
                        label = "dragScale"
                    )

                    val borderColor by animateColorAsState(
                        targetValue = if (isBeingDragged) PureWhite else BorderWhite,
                        label = "dragBorderColor"
                    )

                    val targetOffset = when {
                        dIdx == null -> 0f
                        dIdx < targetIndex && index in (dIdx + 1)..targetIndex -> -itemHeightPx
                        dIdx > targetIndex && index in targetIndex until dIdx -> itemHeightPx
                        else -> 0f
                    }

                    val animatedOffset by animateFloatAsState(
                        targetValue = targetOffset,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "itemShift_$index"
                    )

                    val effectiveTranslationY = if (isBeingDragged) dragOffsetY else animatedOffset

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isBeingDragged) 10f else 1f)
                            .graphicsLayer {
                                translationY = effectiveTranslationY
                                scaleX = scale
                                scaleY = scale
                                shadowElevation = if (isBeingDragged) 16f else 0f
                            }
                            .onGloballyPositioned { coordinates ->
                                if (itemHeightPx == 0f) {
                                    itemHeightPx = coordinates.size.height.toFloat() + with(density) { 10.dp.toPx() }
                                }
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .background(FrostedWhite)
                            .border(
                                BorderStroke(if (isBeingDragged) 1.5.dp else 1.dp, borderColor),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Complication Icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = mode.icon(),
                                contentDescription = mode.displayName,
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Title & Subtitle with Sequence Step Number
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = mode.displayName,
                                    fontFamily = QuicksandFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PureWhite
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val stepNumber = when {
                                    isLocked -> 1
                                    dIdx != null && isBeingDragged -> targetIndex + 1
                                    dIdx != null && dIdx < targetIndex && index in (dIdx + 1)..targetIndex -> index
                                    dIdx != null && dIdx > targetIndex && index in targetIndex until dIdx -> index + 2
                                    else -> index + 1
                                }
                                Text(
                                    text = if (isLocked) "• PINNED #1" else "• #$stepNumber",
                                    fontFamily = QuicksandFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = SubtitleGray,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = mode.description(),
                                fontFamily = ProximaNovaFontFamily,
                                fontSize = 12.sp,
                                color = SubtitleGray
                            )
                        }

                        // Toggle Switch
                        Switch(
                            checked = true,
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

                        Spacer(modifier = Modifier.width(8.dp))

                        // Drag Handle (or Lock icon for Time)
                        if (isLocked) {
                            Box(
                                modifier = Modifier.size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Pinned as first",
                                    tint = SubtitleGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isBeingDragged) Color(0x44FFFFFF) else Color(0x11FFFFFF))
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = {
                                                draggingIndex = index
                                                dragOffsetY = 0f
                                                lastHapticIndex = index
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y
                                                val curr = draggingIndex ?: return@detectDragGestures
                                                if (itemHeightPx > 0f) {
                                                    val newTarget = (curr + (dragOffsetY / itemHeightPx).roundToInt())
                                                        .coerceIn(1, currentComplications.lastIndex)
                                                    if (newTarget != lastHapticIndex) {
                                                        lastHapticIndex = newTarget
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                val from = draggingIndex
                                                if (from != null && from != targetIndex) {
                                                    val mutable = currentComplications.toMutableList()
                                                    val item = mutable.removeAt(from)
                                                    mutable.add(targetIndex, item)
                                                    onReorderUpdated(mutable)
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                                draggingIndex = null
                                                dragOffsetY = 0f
                                                lastHapticIndex = null
                                            },
                                            onDragCancel = {
                                                draggingIndex = null
                                                dragOffsetY = 0f
                                                lastHapticIndex = null
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = "Drag to reorder",
                                    tint = if (isBeingDragged) PureWhite else SubtitleGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. AVAILABLE COMPLICATIONS (Tap switch to activate)
            if (inactiveComplications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "AVAILABLE COMPLICATIONS",
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = SubtitleGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    inactiveComplications.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x11FFFFFF))
                                .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x11FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = mode.icon(),
                                    contentDescription = mode.displayName,
                                    tint = SubtitleGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Title & Description
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.displayName,
                                    fontFamily = QuicksandFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SubtitleGray
                                )
                                Text(
                                    text = mode.description(),
                                    fontFamily = ProximaNovaFontFamily,
                                    fontSize = 12.sp,
                                    color = SubtitleGray.copy(alpha = 0.7f)
                                )
                            }

                            // Switch to activate
                            Switch(
                                checked = false,
                                onCheckedChange = { onToggleComplication(mode) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureWhite,
                                    checkedTrackColor = Color(0x66FFFFFF),
                                    uncheckedThumbColor = SubtitleGray,
                                    uncheckedTrackColor = Color(0x22FFFFFF)
                                )
                            )

                            Spacer(modifier = Modifier.width(44.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
