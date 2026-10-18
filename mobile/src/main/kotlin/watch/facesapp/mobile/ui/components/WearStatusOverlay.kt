package watch.facesapp.mobile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WatchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import watch.facesapp.mobile.data.sync.WearConnectionStatus
import watch.facesapp.mobile.ui.theme.LuxuryBlack
import watch.facesapp.mobile.ui.theme.ProximaNovaFontFamily
import watch.facesapp.mobile.ui.theme.PureWhite
import watch.facesapp.mobile.ui.theme.QuicksandFontFamily

@Composable
fun WearStatusOverlay(
    status: WearConnectionStatus,
    isChecking: Boolean = false,
    onDismiss: () -> Unit,
    onCheckAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xD9080A0D)) // 85% luxury black backdrop scrim
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Modal Card
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF141922))
                .border(
                    BorderStroke(1.dp, Color(0x33FFFFFF)),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume clicks inside dialog */ }
                .padding(24.dp)
        ) {
            // Close 'X' button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0x99FFFFFF)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Status Icon with frosted halo
                val checking = isChecking || status is WearConnectionStatus.Checking
                val iconBg = when (status) {
                    is WearConnectionStatus.ConnectedAndActive -> Color(0x2281C784)
                    is WearConnectionStatus.FaceNotActive -> Color(0x22FFB74D)
                    is WearConnectionStatus.NotConnected -> Color(0x22E57373)
                    WearConnectionStatus.Checking -> Color(0x22FFFFFF)
                }
                val iconBorder = when (status) {
                    is WearConnectionStatus.ConnectedAndActive -> Color(0x6681C784)
                    is WearConnectionStatus.FaceNotActive -> Color(0x66FFB74D)
                    is WearConnectionStatus.NotConnected -> Color(0x66E57373)
                    WearConnectionStatus.Checking -> Color(0x44FFFFFF)
                }

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(iconBg)
                        .border(BorderStroke(1.dp, iconBorder), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (checking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = PureWhite,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        when (status) {
                            is WearConnectionStatus.ConnectedAndActive -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Connected and active",
                                    tint = Color(0xFF81C784),
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            is WearConnectionStatus.FaceNotActive -> {
                                Icon(
                                    imageVector = Icons.Default.Watch,
                                    contentDescription = "Watch face inactive",
                                    tint = Color(0xFFFFB74D),
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            is WearConnectionStatus.NotConnected -> {
                                Icon(
                                    imageVector = Icons.Default.WatchOff,
                                    contentDescription = "Watch not connected",
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            WearConnectionStatus.Checking -> Unit
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Title
                val title = when (status) {
                    is WearConnectionStatus.ConnectedAndActive -> "Watch Ready & Synced"
                    is WearConnectionStatus.FaceNotActive -> "Faces Watch Face Inactive"
                    is WearConnectionStatus.NotConnected -> "Wearable Not Connected"
                    WearConnectionStatus.Checking -> "Checking Wearable..."
                }

                Text(
                    text = title,
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PureWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description & Instructions
                when (status) {
                    is WearConnectionStatus.NotConnected -> {
                        Text(
                            text = "No connected Wear OS device was detected. To see live previews and wear your faces:",
                            fontFamily = ProximaNovaFontFamily,
                            fontSize = 14.sp,
                            color = Color(0xCCFFFFFF),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        InstructionStep(number = "1", text = "Make sure your Wear OS watch is turned on and nearby.")
                        Spacer(modifier = Modifier.height(8.dp))
                        InstructionStep(number = "2", text = "Ensure Bluetooth is enabled and connected via the Wear OS or Galaxy Wearable app.")
                    }
                    is WearConnectionStatus.FaceNotActive -> {
                        val deviceName = status.deviceName
                        Text(
                            text = "Your watch ($deviceName) is connected, but Faces is not currently set as the active watch face:",
                            fontFamily = ProximaNovaFontFamily,
                            fontSize = 14.sp,
                            color = Color(0xCCFFFFFF),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        InstructionStep(number = "1", text = "Press and hold your current watch face on your watch.")
                        Spacer(modifier = Modifier.height(8.dp))
                        InstructionStep(number = "2", text = "Swipe through the list and select Faces.")
                        Spacer(modifier = Modifier.height(8.dp))
                        InstructionStep(number = "3", text = "Live previewing and syncing will instantly connect!")
                    }
                    is WearConnectionStatus.ConnectedAndActive -> {
                        Text(
                            text = "Your Wear OS watch is connected and running Faces. Any changes you make will preview on your wrist in real-time.",
                            fontFamily = ProximaNovaFontFamily,
                            fontSize = 14.sp,
                            color = Color(0xCCFFFFFF),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                    WearConnectionStatus.Checking -> {
                        Text(
                            text = "Communicating with your Wear OS device...",
                            fontFamily = ProximaNovaFontFamily,
                            fontSize = 14.sp,
                            color = Color(0x99FFFFFF),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                FacesPillButton(
                    text = if (checking) "Checking..." else "Check Again",
                    icon = if (!checking) Icons.Default.Refresh else null,
                    onClick = onCheckAgain,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (status is WearConnectionStatus.ConnectedAndActive) "Done" else "Continue Designing",
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xAAFFFFFF),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InstructionStep(
    number: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x1AFFFFFF))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = PureWhite
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontFamily = ProximaNovaFontFamily,
            fontSize = 13.sp,
            color = PureWhite,
            lineHeight = 17.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
