package watch.facesapp.wear.datalayer

import android.content.Context
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceStyle
import watch.facesapp.wear.service.FacesWatchFaceService
import watch.facesapp.wear.service.FacesComplicationDataSourceService
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import androidx.core.content.edit

class WearMessageListener : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == PATH_CURRENT_FACE) {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val styleId = dataMap.getString(KEY_STYLE) ?: FaceStyle.OUA_TIME.id
                    val colorList = dataMap.getIntegerArrayList(KEY_COLORS) ?: arrayListOf()
                    val id = dataMap.getString(KEY_ID) ?: ""
                    val optionsStr = dataMap.getString(KEY_OPTIONS) ?: ""
                    val optionsMap = parseOptions(optionsStr)

                    val newConfig = FaceConfig(
                        id = id,
                        style = FaceStyle.fromId(styleId),
                        colors = colorList,
                        options = optionsMap
                    )

                    saveConfigLocally(newConfig)
                    FacesWatchFaceService.updateConfig(newConfig)

                    try {
                        androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester.create(
                            this@WearMessageListener,
                            android.content.ComponentName(this@WearMessageListener, FacesComplicationDataSourceService::class.java)
                        ).requestUpdateAll()
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            PATH_PREVIEW_START -> {
                FacesWatchFaceService.wakeUpForPreview(applicationContext)
            }
            PATH_PREVIEW_STOP -> {
                FacesWatchFaceService.releasePreviewWakeLock(applicationContext)
            }
            PATH_PREVIEW_PING -> {
                FacesWatchFaceService.refreshPreviewHeartbeat(applicationContext)
            }
            PATH_PREVIEW_FACE -> {
                FacesWatchFaceService.wakeUpForPreview(applicationContext)
                val payload = String(messageEvent.data, Charsets.UTF_8)
                val parts = payload.split("|")
                if (parts.isNotEmpty()) {
                    val styleId = parts[0]
                    val colors = if (parts.size > 1 && parts[1].isNotBlank()) {
                        parts[1].split(",").mapNotNull { it.trim().toIntOrNull() }
                    } else {
                        FaceConfig.defaultColorsForStyle(FaceStyle.fromId(styleId))
                    }

                    val optionsMap = if (parts.size > 2 && parts[2].isNotBlank()) {
                        parseOptions(parts[2])
                    } else {
                        emptyMap()
                    }

                    val previewConfig = FaceConfig(
                        style = FaceStyle.fromId(styleId),
                        colors = colors,
                        options = optionsMap
                    )
                    FacesWatchFaceService.updateConfig(previewConfig)
                }
            }
            PATH_WEATHER -> {
                val payload = String(messageEvent.data, Charsets.UTF_8)
                val parts = payload.split("|")
                if (parts.size >= 2) {
                    val temp = parts[0].toIntOrNull() ?: 21
                    val condition = parts[1]
                    getSharedPreferences("faces_weather_prefs", MODE_PRIVATE).edit {
                        putInt("temp", temp).putString("condition", condition)
                    }
                    FacesWatchFaceService.updateWeather(temp, condition)
                }
            }
            PATH_CALENDAR -> {
                val payload = String(messageEvent.data, Charsets.UTF_8)
                val parts = payload.split("|")
                if (parts.size >= 2) {
                    val title = parts[0]
                    val minutes = parts[1].toIntOrNull() ?: 0
                    getSharedPreferences("faces_calendar_prefs", MODE_PRIVATE).edit {
                        putString("title", title).putInt("minutes", minutes)
                    }
                    FacesWatchFaceService.updateCalendarEvent(title, minutes)
                }
            }
            PATH_STATUS_REQUEST -> {
                val isFaceActive = FacesWatchFaceService.isFaceActive
                val reply = byteArrayOf(if (isFaceActive) 1 else 0)
                com.google.android.gms.wearable.Wearable.getMessageClient(this@WearMessageListener)
                    .sendMessage(messageEvent.sourceNodeId, PATH_STATUS_RESPONSE, reply)
            }
        }
    }

    private fun saveConfigLocally(config: FaceConfig) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit {
            putString(KEY_STYLE, config.style.id)
                .putString(KEY_COLORS, config.colors.joinToString(","))
                .putString(KEY_OPTIONS, config.options.entries.joinToString(";") { "${it.key}=${it.value}" })
                .putString(KEY_ID, config.id)
        }
    }

    companion object {
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
        const val KEY_IS_ACTIVE = "isActive"
        const val KEY_TIMESTAMP = "timestamp"
        const val PREFS_NAME = "wear_face_prefs"

        fun parseOptions(raw: String): Map<String, String> {
            if (raw.isBlank()) return emptyMap()
            return raw.split(";").mapNotNull { entry ->
                val parts = entry.split("=", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }.toMap()
        }

        fun loadSavedConfig(context: Context): FaceConfig {
            val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val styleId = prefs.getString(KEY_STYLE, FaceStyle.OUA_TIME.id) ?: FaceStyle.OUA_TIME.id
            val colorsStr = prefs.getString(KEY_COLORS, "") ?: ""
            val optionsStr = prefs.getString(KEY_OPTIONS, "") ?: ""
            val id = prefs.getString(KEY_ID, "") ?: ""

            val colors = if (colorsStr.isNotBlank()) {
                colorsStr.split(",").mapNotNull { it.trim().toIntOrNull() }
            } else {
                FaceConfig.defaultColorsForStyle(FaceStyle.fromId(styleId))
            }

            return FaceConfig(
                id = id,
                style = FaceStyle.fromId(styleId),
                colors = colors,
                options = parseOptions(optionsStr)
            )
        }
    }
}
