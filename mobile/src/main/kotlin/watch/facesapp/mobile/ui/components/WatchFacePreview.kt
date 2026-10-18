package watch.facesapp.mobile.ui.components

import android.graphics.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import watch.facesapp.core.R
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.core.model.WatchShape
import watch.facesapp.core.render.WatchRenderer
import java.util.Calendar

@Composable
fun WatchFacePreview(
    config: FaceConfig,
    calendar: Calendar,
    isAmbient: Boolean,
    modifier: Modifier = Modifier,
    shape: WatchShape = WatchShape.ROUND,
    faceMode: FaceMode = FaceMode.TIME,
    stepCount: Long = 0L,
    bezelSize: Dp = 230.dp
) {
    val context = LocalContext.current
    val renderer = remember(context) { WatchRenderer(context) }

    val strapRes = if (shape == WatchShape.SQUARE) {
        R.drawable.watch_strap_square
    } else {
        R.drawable.watch_strap
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Watch Strap Graphic
        Image(
            painter = painterResource(id = strapRes),
            contentDescription = "Watch Strap",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        val bounds = remember { Rect() }

        // Live Watch Canvas
        Canvas(modifier = Modifier.size(bezelSize)) {
            drawIntoCanvas { androidCanvas ->
                val nativeCanvas = androidCanvas.nativeCanvas
                bounds.set(0, 0, size.width.toInt(), size.height.toInt())
                renderer.render(
                    canvas = nativeCanvas,
                    bounds = bounds,
                    config = config,
                    calendar = calendar,
                    isAmbient = isAmbient,
                    shape = shape,
                    faceMode = faceMode,
                    stepCount = stepCount
                )
            }
        }
    }
}
