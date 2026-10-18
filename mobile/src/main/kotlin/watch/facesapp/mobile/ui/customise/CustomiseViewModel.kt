package watch.facesapp.mobile.ui.customise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import watch.facesapp.core.model.ColorPalette
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceStyle
import watch.facesapp.mobile.data.db.FaceEntity
import watch.facesapp.mobile.data.db.FacesDatabase
import watch.facesapp.mobile.data.sync.WearSyncManager
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
    ACCENT("ACC", "Accent"),
    TEXT("TXT", "Text");

    companion object {
        fun fromCode(code: String): ColorRole =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: BACKGROUND
    }
}

fun FaceStyle.requiredRoles(): List<ColorRole> =
    template.colorRoles.map { ColorRole.fromCode(it.code) }

class CustomiseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FacesDatabase.getInstance(application)
    private val faceDao = db.faceDao()
    private val syncManager = WearSyncManager(application)

    private val _style = MutableStateFlow(FaceStyle.OUA_TIME)
    val style: StateFlow<FaceStyle> = _style.asStateFlow()

    private val _paletteColors = MutableStateFlow(ColorPalette.defaultPalette())
    val paletteColors: StateFlow<List<Int>> = _paletteColors.asStateFlow()

    private val _options = MutableStateFlow<Map<String, String>>(emptyMap())
    val options: StateFlow<Map<String, String>> = _options.asStateFlow()

    private val _activeComplications = MutableStateFlow<List<watch.facesapp.core.model.FaceMode>>(
        watch.facesapp.core.model.FaceMode.DEFAULT_CYCLE
    )
    val activeComplications: StateFlow<List<watch.facesapp.core.model.FaceMode>> = _activeComplications.asStateFlow()

    private val _previewFaceMode = MutableStateFlow(watch.facesapp.core.model.FaceMode.TIME)
    val previewFaceMode: StateFlow<watch.facesapp.core.model.FaceMode> = _previewFaceMode.asStateFlow()

    // Map each required ColorRole to an index (0..15) into paletteColors
    private val _roleAssignments = MutableStateFlow(
        FaceStyle.OUA_TIME.requiredRoles().mapIndexed { index, role -> role to index }.toMap()
    )
    val roleAssignments: StateFlow<Map<ColorRole, Int>> = _roleAssignments.asStateFlow()

    private val _selectedRole = MutableStateFlow(ColorRole.BACKGROUND)
    val selectedRole: StateFlow<ColorRole> = _selectedRole.asStateFlow()

    val currentConfig: StateFlow<FaceConfig> = combine(_style, _paletteColors, _roleAssignments, _options) { st, pal, roles, opts ->
        val resolvedColors = st.template.colorRoles.map { def ->
            val role = ColorRole.fromCode(def.code)
            val swatchIdx = roles[role]
            if (swatchIdx != null) {
                pal.getOrElse(swatchIdx) { def.defaultColor }
            } else {
                def.defaultColor
            }
        }
        FaceConfig(
            id = UUID.randomUUID().toString(),
            style = st,
            colors = resolvedColors,
            options = opts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = FaceConfig()
    )

    init {
        initAssignmentsForStyle(_style.value)
    }

    private fun initAssignmentsForStyle(st: FaceStyle) {
        val roles = st.requiredRoles()
        val assignments = roles.mapIndexed { idx, role -> role to idx }.toMap()
        _roleAssignments.value = assignments
        _selectedRole.value = roles.firstOrNull() ?: ColorRole.BACKGROUND
    }

    fun setOption(key: String, value: String) {
        _options.value = _options.value + (key to value)
        syncPreview()
    }

    fun cyclePreviewMode() {
        _previewFaceMode.value = _previewFaceMode.value.next(_activeComplications.value)
    }

    fun toggleComplication(mode: watch.facesapp.core.model.FaceMode) {
        if (mode == watch.facesapp.core.model.FaceMode.TIME) return // Time is always locked
        val current = _activeComplications.value.toMutableList()
        if (mode in current) {
            current.remove(mode)
        } else {
            current.add(mode)
        }
        _activeComplications.value = current
        saveComplicationsOption(current)
    }

    fun moveComplicationUp(index: Int) {
        if (index <= 1) return // index 0 is Time
        val current = _activeComplications.value.toMutableList()
        if (index >= current.size) return
        val item = current.removeAt(index)
        current.add(index - 1, item)
        _activeComplications.value = current
        saveComplicationsOption(current)
    }

    fun moveComplicationDown(index: Int) {
        val current = _activeComplications.value.toMutableList()
        if (index < 1 || index >= current.lastIndex) return
        val item = current.removeAt(index)
        current.add(index + 1, item)
        _activeComplications.value = current
        saveComplicationsOption(current)
    }

    private fun saveComplicationsOption(list: List<watch.facesapp.core.model.FaceMode>) {
        val raw = list.joinToString(",") { it.id }
        _options.value = _options.value + ("complications" to raw)
        syncPreview()
    }

    fun initializeWithPalette(colors: List<Int>) {
        if (colors.isNotEmpty()) {
            _paletteColors.value = colors
            shuffleAssignments()
        }
    }

    fun setFixedTemplate(styleIdOrName: String) {
        val styleMatch = FaceStyle.entries.firstOrNull {
            it.id.equals(styleIdOrName, ignoreCase = true) || it.name.equals(styleIdOrName, ignoreCase = true)
        } ?: FaceStyle.OUA_TIME
        _style.value = styleMatch
        _paletteColors.value = ColorPalette.defaultPalette()
        _options.value = emptyMap()
        _activeComplications.value = watch.facesapp.core.model.FaceMode.DEFAULT_CYCLE
        initAssignmentsForStyle(styleMatch)
        shuffleAssignments()
    }

    fun loadExistingFace(faceIdOrStyleId: String) {
        val styleMatch = FaceStyle.entries.firstOrNull { it.id.equals(faceIdOrStyleId, ignoreCase = true) }
        if (styleMatch != null) {
            setFixedTemplate(styleMatch.id)
            return
        }
        viewModelScope.launch {
            faceDao.getFaceById(faceIdOrStyleId).collect { match ->
                if (match != null) {
                    _style.value = match.style
                    _options.value = match.options
                    _activeComplications.value = match.toFaceConfig().activeComplications
                    if (match.colors.isNotEmpty()) {
                        _paletteColors.value = (match.colors + ColorPalette.defaultPalette()).distinct().take(16)
                    }
                    val roles = match.style.requiredRoles()
                    val assignments = roles.mapIndexed { idx, role -> role to idx }.toMap()
                    _roleAssignments.value = assignments
                    _selectedRole.value = roles.first()
                    syncPreview()
                }
            }
        }
    }

    fun setStyle(newStyle: FaceStyle) {
        _style.value = newStyle
        initAssignmentsForStyle(newStyle)
        syncPreview()
    }

    fun selectRole(role: ColorRole) {
        _selectedRole.value = role
    }

    fun assignRoleToSwatch(role: ColorRole, swatchIndex: Int) {
        val updated = _roleAssignments.value.toMutableMap()
        updated[role] = swatchIndex.coerceIn(0, 15)
        _roleAssignments.value = updated
        _selectedRole.value = role
        syncPreview()
    }

    fun assignCurrentRoleToSwatch(swatchIndex: Int) {
        assignRoleToSwatch(_selectedRole.value, swatchIndex)
    }

    fun shuffleAssignments() {
        val roles = _style.value.requiredRoles()
        val availableIndices = (0 until minOf(16, _paletteColors.value.size)).shuffled()
        val updated = mutableMapOf<ColorRole, Int>()
        roles.forEachIndexed { index, role ->
            updated[role] = availableIndices.getOrElse(index) { index }
        }
        _roleAssignments.value = updated
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
            val now = System.currentTimeMillis()
            val entity = FaceEntity.fromFaceConfig(
                config = config,
                name = config.style.displayName,
                lastWornAt = now,
                isCurrentOnWatch = true
            )
            faceDao.insertFace(entity)
            faceDao.recordWearFace(entity.id, now)
            syncManager.syncCurrentFace(config)
            onSaved()
        }
    }
}
