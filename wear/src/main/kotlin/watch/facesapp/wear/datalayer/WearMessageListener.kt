package watch.facesapp.wear.datalayer

import android.content.Context
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceStyle
import watch.facesapp.wear.service.FacesWatchFaceService
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

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

                    val newConfig = FaceConfig(
                        id = id,
                        style = FaceStyle.fromId(styleId),
                        colors = colorList
                    )

                    saveConfigLocally(newConfig)
                    FacesWatchFaceService.updateConfig(newConfig)
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == PATH_PREVIEW_FACE) {
            val payload = String(messageEvent.data, Charsets.UTF_8)
            val parts = payload.split("|")
            if (parts.isNotEmpty()) {
                val styleId = parts[0]
                val colors = if (parts.size > 1 && parts[1].isNotBlank()) {
                    parts[1].split(",").mapNotNull { it.trim().toIntOrNull() }
                } else {
                    FaceConfig.defaultColorsForStyle(FaceStyle.fromId(styleId))
                }

                val previewConfig = FaceConfig(
                    style = FaceStyle.fromId(styleId),
                    colors = colors
                )
                FacesWatchFaceService.updateConfig(previewConfig)
            }
        }
    }

    private fun saveConfigLocally(config: FaceConfig) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_STYLE, config.style.id)
            .putString(KEY_COLORS, config.colors.joinToString(","))
            .putString(KEY_ID, config.id)
            .apply()
    }

    companion object {
        const val PATH_CURRENT_FACE = "/faces/current"
        const val PATH_PREVIEW_FACE = "/faces/preview"
        const val KEY_ID = "id"
        const val KEY_STYLE = "style"
        const val KEY_COLORS = "colors"
        const val PREFS_NAME = "wear_face_prefs"

        fun loadSavedConfig(context: Context): FaceConfig {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val styleId = prefs.getString(KEY_STYLE, FaceStyle.OUA_TIME.id) ?: FaceStyle.OUA_TIME.id
            val colorsStr = prefs.getString(KEY_COLORS, "") ?: ""
            val id = prefs.getString(KEY_ID, "") ?: ""

            val colors = if (colorsStr.isNotBlank()) {
                colorsStr.split(",").mapNotNull { it.trim().toIntOrNull() }
            } else {
                FaceConfig.defaultColorsForStyle(FaceStyle.fromId(styleId))
            }

            return FaceConfig(
                id = id,
                style = FaceStyle.fromId(styleId),
                colors = colors
            )
        }
    }
}
