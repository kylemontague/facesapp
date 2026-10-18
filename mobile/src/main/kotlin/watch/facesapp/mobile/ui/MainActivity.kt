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
import kotlinx.coroutines.launch
import watch.facesapp.mobile.data.sync.WearSyncManager
import watch.facesapp.mobile.ui.theme.FacesTheme

class MainActivity : ComponentActivity() {

    private val deepLinkUriState = MutableStateFlow<Uri?>(null)
    private val syncManager by lazy { WearSyncManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }

    override fun onPause() {
        super.onPause()
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
