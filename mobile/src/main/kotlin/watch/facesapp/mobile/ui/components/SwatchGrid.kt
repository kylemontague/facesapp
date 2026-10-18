package watch.facesapp.mobile.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import watch.facesapp.core.util.ColorContrast
import watch.facesapp.mobile.ui.customise.ColorRole
import watch.facesapp.mobile.ui.theme.BorderWhite
import watch.facesapp.mobile.ui.theme.PureWhite
import watch.facesapp.mobile.ui.theme.QuicksandFontFamily

data class SwatchMarker(
    val role: ColorRole,
    val colorIndex: Int
)

@Composable
fun SwatchGrid(
    paletteColors: List<Int>,
    markers: List<SwatchMarker>,
    selectedRole: ColorRole,
    onRoleSelected: (ColorRole) -> Unit,
    onRoleAssigned: (ColorRole, Int) -> Unit,
    onSwatchClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val currentMarkers by rememberUpdatedState(markers)
    val currentSelectedRole by rememberUpdatedState(selectedRole)
    val currentOnRoleSelected by rememberUpdatedState(onRoleSelected)
    val currentOnRoleAssigned by rememberUpdatedState(onRoleAssigned)
    val currentOnSwatchClick by rememberUpdatedState(onSwatchClick)

    var isDragging by remember { mutableStateOf(false) }
    var activeDragRole by remember { mutableStateOf<ColorRole?>(null) }
    var activeDragSwatchIndex by remember { mutableIntStateOf(-1) }

    val spacingDp = 14.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                val spacingPx = spacingDp.toPx()

                fun getSwatchCenter(index: Int, swatchSize: Float, step: Float): Offset {
                    val col = index % 4
                    val row = index / 4
                    val cx = col * step + swatchSize / 2f
                    val cy = row * step + swatchSize / 2f
                    return Offset(cx, cy)
                }

                fun findClosestSwatch(offset: Offset, swatchSize: Float, step: Float): Int {
                    var closest = 0
                    var minDistanceSq = Float.MAX_VALUE
                    for (i in 0 until 16) {
                        val center = getSwatchCenter(i, swatchSize, step)
                        val dx = center.x - offset.x
                        val dy = center.y - offset.y
                        val distSq = dx * dx + dy * dy
                        if (distSq < minDistanceSq) {
                            minDistanceSq = distSq
                            closest = i
                        }
                    }
                    return closest
                }

                fun findTouchedSwatch(offset: Offset, swatchSize: Float, step: Float): Int? {
                    val closest = findClosestSwatch(offset, swatchSize, step)
                    val center = getSwatchCenter(closest, swatchSize, step)
                    val dx = center.x - offset.x
                    val dy = center.y - offset.y
                    val touchRadius = swatchSize * 0.75f
                    return if (dx * dx + dy * dy <= touchRadius * touchRadius) closest else null
                }

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val gridWidth = size.width.toFloat()
                    val swatchSize = (gridWidth - 3f * spacingPx) / 4f
                    val step = swatchSize + spacingPx

                    val startOffset = down.position
                    val startIndex = findTouchedSwatch(startOffset, swatchSize, step) ?: return@awaitEachGesture

                    val matchingMarkers = currentMarkers.filter { it.colorIndex == startIndex }
                    val hasMarker = matchingMarkers.isNotEmpty()

                    val candidateRole = if (hasMarker) {
                        matchingMarkers.firstOrNull { it.role == currentSelectedRole }?.role
                            ?: matchingMarkers.first().role
                    } else {
                        null
                    }

                    var dragStarted = false
                    var currentTargetIndex = startIndex
                    val touchSlop = viewConfiguration.touchSlop

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (change.isConsumed) break

                        if (change.pressed) {
                            val currentOffset = change.position
                            val distance = (currentOffset - startOffset).getDistance()

                            if (!dragStarted && hasMarker && candidateRole != null && distance > touchSlop) {
                                dragStarted = true
                                isDragging = true
                                activeDragRole = candidateRole
                                activeDragSwatchIndex = startIndex
                                currentOnRoleSelected(candidateRole)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }

                            if (dragStarted && candidateRole != null) {
                                change.consume()
                                val closest = findClosestSwatch(currentOffset, swatchSize, step)
                                if (closest != currentTargetIndex) {
                                    currentTargetIndex = closest
                                    activeDragSwatchIndex = closest
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    currentOnRoleAssigned(candidateRole, closest)
                                }
                            }
                        } else {
                            // Pointer released
                            change.consume()
                            if (dragStarted && candidateRole != null) {
                                currentOnRoleAssigned(candidateRole, currentTargetIndex)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            } else {
                                // Tap interaction
                                if (hasMarker && candidateRole != null) {
                                    currentOnRoleSelected(candidateRole)
                                } else {
                                    currentOnSwatchClick(startIndex)
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            break
                        }
                    }

                    isDragging = false
                    activeDragRole = null
                    activeDragSwatchIndex = -1
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacingDp)
        ) {
            for (row in 0 until 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacingDp)
                ) {
                    for (col in 0 until 4) {
                        val index = row * 4 + col
                        val colorInt = paletteColors.getOrElse(index) { 0xFF080A0D.toInt() }
                        val color = Color(colorInt)
                        val matchedMarkers = markers.filter { it.colorIndex == index }
                        val isSelected = matchedMarkers.isNotEmpty()
                        val isBeingDragged = isDragging && (index == activeDragSwatchIndex)

                        val scale by animateFloatAsState(
                            targetValue = when {
                                isBeingDragged -> 1.18f
                                isSelected -> 1.05f
                                else -> 1.0f
                            },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "swatchScale"
                        )

                        val borderWidth by animateDpAsState(
                            targetValue = when {
                                isBeingDragged -> 3.5.dp
                                isSelected -> 2.5.dp
                                else -> 0.dp
                            },
                            label = "borderWidth"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .zIndex(if (isBeingDragged) 2f else if (isSelected) 1f else 0f)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    BorderStroke(
                                        width = if (isSelected || isBeingDragged) borderWidth else 0.5.dp,
                                        color = when {
                                            isBeingDragged -> PureWhite
                                            isSelected -> BorderWhite
                                            else -> Color(0x33FFFFFF)
                                        }
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (matchedMarkers.isNotEmpty()) {
                                val label = matchedMarkers.joinToString("/") { it.role.code }
                                val textColor = if (ColorContrast.getContrastingTextColor(colorInt) == 0xFFFFFFFF.toInt()) {
                                    Color.White
                                } else {
                                    Color.Black
                                }

                                Text(
                                    text = label,
                                    fontFamily = QuicksandFontFamily,
                                    fontSize = 11.sp,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
