package watch.facesapp.mobile.data.prefs

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import watch.facesapp.core.model.FaceMode

class ComplicationsRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _activeComplications = MutableStateFlow(loadFromPrefs())
    val activeComplications: StateFlow<List<FaceMode>> = _activeComplications.asStateFlow()

    private fun loadFromPrefs(): List<FaceMode> {
        val raw = prefs.getString(KEY_COMPLICATIONS, null)
        if (raw.isNullOrBlank()) {
            return FaceMode.DEFAULT_CYCLE
        }
        val parsed = raw.split(",").mapNotNull { FaceMode.fromId(it.trim()) }
        if (parsed.isEmpty()) {
            return FaceMode.DEFAULT_CYCLE
        }
        return if (FaceMode.TIME in parsed) {
            listOf(FaceMode.TIME) + parsed.filter { it != FaceMode.TIME }
        } else {
            listOf(FaceMode.TIME) + parsed
        }
    }

    fun setComplications(list: List<FaceMode>) {
        val sanitized = if (FaceMode.TIME in list) {
            listOf(FaceMode.TIME) + list.filter { it != FaceMode.TIME }
        } else {
            listOf(FaceMode.TIME) + list
        }
        val serialized = sanitized.joinToString(",") { it.id }
        prefs.edit { putString(KEY_COMPLICATIONS, serialized) }
        _activeComplications.value = sanitized
    }

    fun toggleComplication(mode: FaceMode) {
        if (mode == FaceMode.TIME) return
        val current = _activeComplications.value.toMutableList()
        if (mode in current) {
            current.remove(mode)
        } else {
            current.add(mode)
        }
        setComplications(current)
    }

    companion object {
        private const val PREFS_NAME = "universal_complications_prefs"
        private const val KEY_COMPLICATIONS = "active_complications"

        @Volatile
        private var INSTANCE: ComplicationsRepository? = null

        fun getInstance(context: Context): ComplicationsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ComplicationsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
