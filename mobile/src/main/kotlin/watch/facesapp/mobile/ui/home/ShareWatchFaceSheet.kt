package watch.facesapp.mobile.ui.home

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import watch.facesapp.core.util.FaceShareUtils
import watch.facesapp.mobile.data.db.FaceEntity
import watch.facesapp.mobile.ui.components.FacesPillButton
import watch.facesapp.mobile.ui.theme.BorderWhite
import watch.facesapp.mobile.ui.theme.FrostedWhite
import watch.facesapp.mobile.ui.theme.LuxuryBlack
import watch.facesapp.mobile.ui.theme.ProximaNovaFontFamily
import watch.facesapp.mobile.ui.theme.PureWhite
import watch.facesapp.mobile.ui.theme.QuicksandFontFamily
import watch.facesapp.mobile.ui.theme.SubtitleGray
import watch.facesapp.mobile.util.QrCodeGenerator
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareWatchFaceSheet(
    face: FaceEntity,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val shareUrl = remember(face) {
        FaceShareUtils.createShareUrl(
            style = face.style,
            colors = face.colors,
            name = face.name
        )
    }

    val qrBitmap = remember(shareUrl) {
        QrCodeGenerator.generateQrBitmap(content = shareUrl, sizePx = 512)
    }

    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2500L.milliseconds)
            isCopied = false
        }
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
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SHARE CREATION",
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 1.2.sp,
                        color = PureWhite
                    )
                    Text(
                        text = face.name,
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

            Spacer(modifier = Modifier.height(20.dp))

            // QR Code Card
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Watch Face QR Code",
                        modifier = Modifier.size(206.dp)
                    )
                } else {
                    Text(
                        text = "Generating QR Code...",
                        color = Color.Black,
                        fontFamily = ProximaNovaFontFamily,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Color Swatches Row
            if (face.colors.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    face.colors.forEach { colorInt ->
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .border(1.dp, PureWhite.copy(alpha = 0.5f), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = "Scan with any camera or QR reader to import directly to another device",
                fontFamily = ProximaNovaFontFamily,
                fontSize = 13.sp,
                color = SubtitleGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Copy Link Button
                Box(modifier = Modifier.weight(1f)) {
                    FacesPillButton(
                        text = if (isCopied) "Link Copied!" else "Copy Share Link",
                        icon = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        onClick = {
                            clipboardManager.setText(AnnotatedString(shareUrl))
                            isCopied = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Native Share Sheet Button
                IconButton(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Faces Watch Face: ${face.name}")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Check out my custom watch face '${face.name}' for Faces: $shareUrl"
                            )
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Watch Face")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(FrostedWhite)
                        .border(BorderStroke(1.dp, BorderWhite), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share via other apps",
                        tint = PureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
