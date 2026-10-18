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

    /**
     * Wakes up the watch face and requests interactive active display mode for live customization.
     */
    suspend fun startPreviewSession(): Result<Unit> {
        return runCatching {
            val nodes = nodeClient.connectedNodes.await()
            for (node in nodes) {
                messageClient.sendMessage(node.id, PATH_PREVIEW_START, ByteArray(0)).await()
            }
            Log.d(TAG, "Sent preview start signal to ${nodes.size} node(s)")
            Unit
        }.onFailure { e ->
            Log.e(TAG, "Failed to send preview start signal to Wear OS", e)
        }
    }

    /**
     * Releases the wake lock on the watch and lets it return to its normal ambient/sleep cycle.
     */
    suspend fun stopPreviewSession(): Result<Unit> {
        return runCatching {
            val nodes = nodeClient.connectedNodes.await()
            for (node in nodes) {
                messageClient.sendMessage(node.id, PATH_PREVIEW_STOP, ByteArray(0)).await()
            }
            Log.d(TAG, "Sent preview stop signal to ${nodes.size} node(s)")
            Unit
        }.onFailure { e ->
            Log.e(TAG, "Failed to send preview stop signal to Wear OS", e)
        }
    }

    /**
     * Synchronizes live weather temperature and condition to connected Wear OS watch.
     */
    suspend fun syncWeather(tempC: Int, condition: String): Result<Unit> {
        return runCatching {
            val nodes = nodeClient.connectedNodes.await()
            val payload = "$tempC|$condition"
            val bytes = payload.toByteArray(Charsets.UTF_8)
            for (node in nodes) {
                messageClient.sendMessage(node.id, PATH_WEATHER, bytes).await()
            }
            Log.d(TAG, "Sent live weather ($tempC°C, $condition) to ${nodes.size} node(s)")
            Unit
        }.onFailure { e ->
            Log.e(TAG, "Failed to send weather update to Wear OS", e)
        }
    }

    /**
     * Synchronizes next calendar appointment title and minutes until event to connected Wear OS watch.
     */
    suspend fun syncCalendarEvent(title: String, minutesUntil: Int): Result<Unit> {
        return runCatching {
            val nodes = nodeClient.connectedNodes.await()
            val payload = "$title|$minutesUntil"
            val bytes = payload.toByteArray(Charsets.UTF_8)
            for (node in nodes) {
                messageClient.sendMessage(node.id, PATH_CALENDAR, bytes).await()
            }
            Log.d(TAG, "Sent calendar event ($title in ${minutesUntil}m) to ${nodes.size} node(s)")
            Unit
        }.onFailure { e ->
            Log.e(TAG, "Failed to send calendar update to Wear OS", e)
        }
    }

    /**
     * Transmits a lightweight keep-alive ping to reset the watch preview auto-revert timer.
     */
    suspend fun sendPreviewPing(): Result<Unit> {
        return runCatching {
            val nodes = nodeClient.connectedNodes.await()
            for (node in nodes) {
                messageClient.sendMessage(node.id, PATH_PREVIEW_PING, ByteArray(0)).await()
            }
            Unit
        }
    }

    /**
     * Checks if a Wear OS device is connected and whether the Faces watch face is active.
     */
    suspend fun checkWearStatus(): WearConnectionStatus = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val nodes = nodeClient.connectedNodes.await()
            if (nodes.isEmpty()) {
                Log.d(TAG, "checkWearStatus: No connected nodes found")
                return@withContext WearConnectionStatus.NotConnected()
            }

            val primaryNode = nodes.first()
            val deviceName = primaryNode.displayName.ifBlank { "Wear OS Watch" }

            val responseDeferred = kotlinx.coroutines.CompletableDeferred<Boolean>()
            val listener = com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener { event ->
                if (event.path == PATH_STATUS_RESPONSE) {
                    val isActive = event.data.isNotEmpty() && event.data[0] == 1.toByte()
                    responseDeferred.complete(isActive)
                }
            }

            messageClient.addListener(listener)
            try {
                messageClient.sendMessage(primaryNode.id, PATH_STATUS_REQUEST, ByteArray(0)).await()
                val isActive = kotlinx.coroutines.withTimeoutOrNull(1500L) {
                    responseDeferred.await()
                }

                if (isActive == true) {
                    Log.d(TAG, "checkWearStatus: Node $deviceName is connected and Faces is active")
                    WearConnectionStatus.ConnectedAndActive
                } else if (isActive == false) {
                    Log.d(TAG, "checkWearStatus: Node $deviceName is connected but Faces is inactive")
                    WearConnectionStatus.FaceNotActive(deviceName)
                } else {
                    val dataActive = checkDataClientStatus()
                    Log.d(TAG, "checkWearStatus: Ping timed out, DataClient fallback=$dataActive")
                    if (dataActive == true) {
                        WearConnectionStatus.ConnectedAndActive
                    } else {
                        WearConnectionStatus.FaceNotActive(deviceName)
                    }
                }
            } finally {
                messageClient.removeListener(listener)
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkWearStatus: Error checking status", e)
            WearConnectionStatus.NotConnected(e.message ?: "Connection check failed")
        }
    }

    private suspend fun checkDataClientStatus(): Boolean? {
        return try {
            val uri = android.net.Uri.Builder().scheme(com.google.android.gms.wearable.PutDataRequest.WEAR_URI_SCHEME).path(PATH_WATCH_STATUS).build()
            val dataItem = dataClient.getDataItem(uri).await() ?: return null
            val dataMap = com.google.android.gms.wearable.DataMapItem.fromDataItem(dataItem).dataMap
            val timestamp = dataMap.getLong(KEY_TIMESTAMP)
            if (System.currentTimeMillis() - timestamp < 24 * 60 * 60 * 1000L) {
                dataMap.getBoolean(KEY_IS_ACTIVE, false)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun buildPreviewPayload(config: FaceConfig): String {
        val colorsStr = config.colors.joinToString(",")
        val optionsStr = config.options.entries.joinToString(";") { "${it.key}=${it.value}" }
        return "${config.style.id}|$colorsStr|$optionsStr"
    }

    companion object {
        private const val TAG = "WearSyncManager"
        const val PATH_CURRENT_FACE = "/faces/current"
        const val PATH_PREVIEW_FACE = "/faces/preview"
        const val PATH_PREVIEW_START = "/faces/preview/start"
        const val PATH_PREVIEW_STOP = "/faces/preview/stop"
        const val PATH_PREVIEW_PING = "/faces/preview/ping"
        const val PATH_WEATHER = "/faces/weather"
        const val PATH_CALENDAR = "/faces/calendar"
        const val PATH_STATUS_REQUEST = "/faces/status/request"
        const val PATH_STATUS_RESPONSE = "/faces/status/response"
        const val PATH_WATCH_STATUS = "/faces/status"

        const val KEY_ID = "id"
        const val KEY_STYLE = "style"
        const val KEY_COLORS = "colors"
        const val KEY_OPTIONS = "options"
        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_IS_ACTIVE = "isActive"
    }
}

sealed class WearConnectionStatus {
    object Checking : WearConnectionStatus()
    object ConnectedAndActive : WearConnectionStatus()
    data class NotConnected(val message: String = "No Wear OS device connected") : WearConnectionStatus()
    data class FaceNotActive(val deviceName: String = "Wear OS Watch") : WearConnectionStatus()
}
