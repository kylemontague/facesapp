package com.faces.mobile.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faces.mobile.data.db.FaceWearLogEntity
import com.faces.mobile.ui.theme.BorderWhite
import com.faces.mobile.ui.theme.FrostedWhite
import com.faces.mobile.ui.theme.LuxuryBlack
import com.faces.mobile.ui.theme.ProximaNovaFontFamily
import com.faces.mobile.ui.theme.PureWhite
import com.faces.mobile.ui.theme.QuicksandFontFamily
import com.faces.mobile.ui.theme.SubtitleGray
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearTimelineSheet(
    faceName: String,
    wearLogs: List<FaceWearLogEntity>,
    stats: FaceStats? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WEAR TIMELINE",
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 1.2.sp,
                        color = PureWhite
                    )
                    Text(
                        text = faceName,
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

            // Subtitle badges (Wear Count, Total Duration, Most Worn)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(FrostedWhite, RoundedCornerShape(50))
                        .border(BorderStroke(0.5.dp, Color(0x44FFFFFF)), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (wearLogs.isEmpty()) "Never worn" else "Worn ${wearLogs.size} ${if (wearLogs.size == 1) "time" else "times"}",
                        fontFamily = QuicksandFontFamily,
                        fontSize = 12.sp,
                        color = PureWhite
                    )
                }

                if (stats != null && stats.totalDurationMs > 0L) {
                    Box(
                        modifier = Modifier
                            .background(FrostedWhite, RoundedCornerShape(50))
                            .border(BorderStroke(0.5.dp, Color(0x44FFFFFF)), RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Total: ${stats.formattedDuration}",
                            fontFamily = QuicksandFontFamily,
                            fontSize = 12.sp,
                            color = PureWhite
                        )
                    }
                }

                if (stats != null && stats.isMostWorn) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFD9A035).copy(alpha = 0.25f), RoundedCornerShape(50))
                            .border(BorderStroke(0.5.dp, Color(0xFFD9A035)), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "★ Most Worn",
                            fontFamily = QuicksandFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD566)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (wearLogs.isEmpty()) {
                Text(
                    text = "No recorded wear sessions yet. Tap 'WEAR AGAIN' to wear this watch.",
                    fontFamily = ProximaNovaFontFamily,
                    fontSize = 14.sp,
                    color = SubtitleGray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    itemsIndexed(wearLogs) { index, log ->
                        val isLatest = index == 0 && log.wornUntil == null
                        val dateStr = DateFormat.getLongDateFormat(context).format(Date(log.wornAt))
                        val timeStr = DateFormat.getTimeFormat(context).format(Date(log.wornAt))
                        val sessionDuration = if (log.wornUntil != null) {
                            "Active for ${formatDuration(log.wornUntil - log.wornAt)}"
                        } else {
                            "Currently on wrist"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Timeline dot
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(if (isLatest) Color(0xFF34A5A4) else FrostedWhite)
                                    .border(
                                        BorderStroke(1.dp, if (isLatest) PureWhite else Color(0x66FFFFFF)),
                                        CircleShape
                                    )
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$dateStr at $timeStr",
                                        fontFamily = ProximaNovaFontFamily,
                                        fontWeight = if (isLatest) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 15.sp,
                                        color = PureWhite
                                    )
                                    if (isLatest) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF34A5A4).copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                                .border(BorderStroke(0.5.dp, Color(0xFF34A5A4)), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontFamily = QuicksandFontFamily,
                                                fontSize = 9.sp,
                                                color = Color(0xFF34A5A4),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = sessionDuration,
                                    fontFamily = ProximaNovaFontFamily,
                                    fontSize = 12.sp,
                                    color = SubtitleGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
