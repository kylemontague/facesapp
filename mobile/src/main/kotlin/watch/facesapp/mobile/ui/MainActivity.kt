package watch.facesapp.mobile.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import watch.facesapp.mobile.ui.theme.FacesTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val deepLinkUriState = MutableStateFlow<Uri?>(null)

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri ->
            deepLinkUriState.value = uri
        }
    }
}
