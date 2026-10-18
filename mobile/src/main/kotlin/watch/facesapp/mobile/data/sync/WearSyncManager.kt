package watch.facesapp.mobile.data.sync

import android.content.Context
import android.util.Log
import watch.facesapp.core.model.FaceConfig
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearSyncManager(context: Context) {

    private val dataClient = Wearable.getDataClient(context)
    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)

    /**
     * Persistently synchronizes the active watchface configuration to Wear OS via DataClient.
     */
    suspend fun syncCurrentFace(config: FaceConfig): Result<Unit> {
        return runCatching {
            val putDataMapRequest = PutDataMapRequest.create(PATH_CURRENT_FACE).apply {
                dataMap.putString(KEY_ID, config.id)
                dataMap.putString(KEY_STYLE, config.style.id)
                dataMap.putIntegerArrayList(KEY_COLORS, ArrayList(config.colors))
                dataMap.putString(KEY_OPTIONS, config.options.entries.joinToString(";") { "${it.key}=${it.value}" })
                dataMap.putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            }

            val request = putDataMapRequest.asPutDataRequest().setUrgent()
            dataClient.putDataItem(request).await()
            Log.d(TAG, "Successfully synced face ${config.id} to Wear OS")
            Unit
        }.onFailure { e ->
            Log.e(TAG, "Failed to sync face ${config.id} to Wear OS", e)
        }
    }

    /**
     * Sends a real-time message to connected watch nodes for instantaneous preview updates.
     */
    suspend fun sendPreviewUpdate(config: FaceConfig): Result<Unit> {
        return runCatching {
            val nodes = nodeClient.connectedNodes.await()
            if (nodes.isEmpty()) {
                Log.d(TAG, "No connected Wear OS nodes found for preview message")
                return@runCatching
            }

            val payload = buildPreviewPayload(config)
            val bytes = payload.toByteArray(Charsets.UTF_8)

            for (node in nodes) {
                messageClient.sendMessage(node.id, PATH_PREVIEW_FACE, bytes).await()
            }
            Log.d(TAG, "Sent preview update to ${nodes.size} node(s)")
            Unit
        }.onFailure { e ->
            Log.e(TAG, "Failed to send preview message to Wear OS", e)
        }
    }

    private fun buildPreviewPayload(config: FaceConfig): String {
        val colorsStr = config.colors.joinToString(",")
        return "${config.style.id}|$colorsStr"
    }

    companion object {
        private const val TAG = "WearSyncManager"
        const val PATH_CURRENT_FACE = "/faces/current"
        const val PATH_PREVIEW_FACE = "/faces/preview"

        const val KEY_ID = "id"
        const val KEY_STYLE = "style"
        const val KEY_COLORS = "colors"
        const val KEY_OPTIONS = "options"
        const val KEY_TIMESTAMP = "timestamp"
    }
}
