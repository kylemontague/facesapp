package watch.facesapp.mobile.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import watch.facesapp.mobile.data.sync.WearSyncManager
import watch.facesapp.mobile.ui.theme.FacesTheme
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {

    private val deepLinkUriState = MutableStateFlow<Uri?>(null)
    private val syncManager by lazy { WearSyncManager(applicationContext) }
    private var previewHeartbeatJob: Job? = null

    private val activityRecognitionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { /* Permission granted or denied: PhoneHealthManager automatically listens or uses cached fallback */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACTIVITY_RECOGNITION
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                activityRecognitionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        intent?.data?.let { uri ->
            deepLinkUriState.value = uri
        }

        setContent {
            val deepLinkUri by deepLinkUriState.collectAsState()
            FacesTheme {
                FacesApp(
                    deepLinkUri = deepLinkUri,
                    onClearDeepLink = { deepLinkUriState.value = null }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            syncManager.startPreviewSession()
        }
        previewHeartbeatJob?.cancel()
        previewHeartbeatJob = lifecycleScope.launch {
            while (isActive) {
                delay(15000L.milliseconds)
                syncManager.sendPreviewPing()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        previewHeartbeatJob?.cancel()
        previewHeartbeatJob = null
        lifecycleScope.launch {
            syncManager.stopPreviewSession()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri ->
            deepLinkUriState.value = uri
        }
    }
}
