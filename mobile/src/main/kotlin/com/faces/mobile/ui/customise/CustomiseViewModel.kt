package com.faces.mobile.ui.customise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.faces.core.model.ColorPalette
import com.faces.core.model.FaceConfig
import com.faces.core.model.FaceStyle
import com.faces.mobile.data.db.FaceEntity
import com.faces.mobile.data.db.FacesDatabase
import com.faces.mobile.data.sync.WearSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class ColorRole(val code: String, val displayName: String) {
    BACKGROUND("BG", "Background"),
    HOUR("HR", "Hour"),
    MINUTE("MIN", "Minute"),
    SECOND("SEC", "Second"),
    ACCENT("ACC", "Accent")
}

class CustomiseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FacesDatabase.getInstance(application)
    private val faceDao = db.faceDao()
    private val syncManager = WearSyncManager(application)

    private val _style = MutableStateFlow(FaceStyle.OUA_TIME)
    val style: StateFlow<FaceStyle> = _style.asStateFlow()

    private val _paletteColors = MutableStateFlow(ColorPalette.defaultPalette())
    val paletteColors: StateFlow<List<Int>> = _paletteColors.asStateFlow()

    // Map each ColorRole to an index (0..15) into paletteColors
    private val _roleAssignments = MutableStateFlow(
        mapOf(
            ColorRole.BACKGROUND to 0,
            ColorRole.HOUR to 1,
            ColorRole.MINUTE to 2,
            ColorRole.SECOND to 3,
            ColorRole.ACCENT to 4
        )
    )
    val roleAssignments: StateFlow<Map<ColorRole, Int>> = _roleAssignments.asStateFlow()

    private val _selectedRole = MutableStateFlow(ColorRole.BACKGROUND)
    val selectedRole: StateFlow<ColorRole> = _selectedRole.asStateFlow()

    val currentConfig: StateFlow<FaceConfig> = combine(_style, _paletteColors, _roleAssignments) { st, pal, roles ->
        val resolvedColors = listOf(
            pal.getOrElse(roles[ColorRole.BACKGROUND] ?: 0) { 0xFF080A0D.toInt() },
            pal.getOrElse(roles[ColorRole.HOUR] ?: 1) { 0xFFFFFFFF.toInt() },
            pal.getOrElse(roles[ColorRole.MINUTE] ?: 2) { 0xFF55EE45.toInt() },
            pal.getOrElse(roles[ColorRole.SECOND] ?: 3) { 0xFF3C797F.toInt() },
            pal.getOrElse(roles[ColorRole.ACCENT] ?: 4) { 0xFFCDE9E6.toInt() },
            pal.getOrElse(roles[ColorRole.HOUR] ?: 1) { 0xFFFFFFFF.toInt() }
        )
        FaceConfig(
            id = UUID.randomUUID().toString(),
            style = st,
            colors = resolvedColors
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = FaceConfig()
    )

    fun initializeWithPalette(colors: List<Int>) {
        if (colors.isNotEmpty()) {
            _paletteColors.value = colors
            shuffleAssignments()
        }
    }

    fun loadExistingFace(faceId: String) {
        viewModelScope.launch {
            val face = faceDao.getAllFaces()
            // find by id
            face.collect { list ->
                val match = list.firstOrNull { it.id == faceId }
                if (match != null) {
                    _style.value = match.style
                    if (match.colors.isNotEmpty()) {
                        _paletteColors.value = (match.colors + ColorPalette.defaultPalette()).distinct().take(16)
                    }
                }
            }
        }
    }

    fun setStyle(newStyle: FaceStyle) {
        _style.value = newStyle
        syncPreview()
    }

    fun selectRole(role: ColorRole) {
        _selectedRole.value = role
    }

    fun assignCurrentRoleToSwatch(swatchIndex: Int) {
        val updated = _roleAssignments.value.toMutableMap()
        updated[_selectedRole.value] = swatchIndex
        _roleAssignments.value = updated
        syncPreview()
    }

    fun shuffleAssignments() {
        val availableIndices = (0 until minOf(16, _paletteColors.value.size)).shuffled()
        _roleAssignments.value = mapOf(
            ColorRole.BACKGROUND to availableIndices.getOrElse(0) { 0 },
            ColorRole.HOUR to availableIndices.getOrElse(1) { 1 },
            ColorRole.MINUTE to availableIndices.getOrElse(2) { 2 },
            ColorRole.SECOND to availableIndices.getOrElse(3) { 3 },
            ColorRole.ACCENT to availableIndices.getOrElse(4) { 4 }
        )
        syncPreview()
    }

    private fun syncPreview() {
        viewModelScope.launch {
            syncManager.sendPreviewUpdate(currentConfig.value)
        }
    }

    fun saveFace(onSaved: () -> Unit) {
        viewModelScope.launch {
            val config = currentConfig.value
            val entity = FaceEntity.fromFaceConfig(config, name = config.style.displayName)
            faceDao.insertFace(entity)
            syncManager.syncCurrentFace(config)
            onSaved()
        }
    }
}
