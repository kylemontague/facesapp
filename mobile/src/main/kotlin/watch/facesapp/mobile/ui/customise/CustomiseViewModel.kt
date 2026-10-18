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
import kotlinx.coroutines.flow.map
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

    private val complicationsRepo = watch.facesapp.mobile.data.prefs.ComplicationsRepository.getInstance(application)
    val activeComplications: StateFlow<List<watch.facesapp.core.model.FaceMode>> = complicationsRepo.activeComplications

    private val _previewFaceMode = MutableStateFlow(watch.facesapp.core.model.FaceMode.TIME)
    val previewFaceMode: StateFlow<watch.facesapp.core.model.FaceMode> = _previewFaceMode.asStateFlow()

    // Map each required ColorRole to an index (0..15) into paletteColors
    private val _roleAssignments = MutableStateFlow(
        FaceStyle.OUA_TIME.requiredRoles().mapIndexed { index, role -> role to index }.toMap()
    )
    val roleAssignments: StateFlow<Map<ColorRole, Int>> = _roleAssignments.asStateFlow()

    private val _selectedRole = MutableStateFlow(ColorRole.BACKGROUND)
    val selectedRole: StateFlow<ColorRole> = _selectedRole.asStateFlow()

    private val _contrastGuardMode = MutableStateFlow(watch.facesapp.core.model.ContrastGuardMode.SOFT)
    val contrastGuardMode: StateFlow<watch.facesapp.core.model.ContrastGuardMode> = _contrastGuardMode.asStateFlow()

    data class ContrastEvaluation(
        val statuses: Map<Int, watch.facesapp.core.model.SwatchContrastStatus>,
        val isRelaxed: Boolean
    )

    private val contrastEvaluation: StateFlow<ContrastEvaluation> = combine(
        _paletteColors,
        _roleAssignments,
        _selectedRole,
        _style,
        _contrastGuardMode
    ) { pal, roles, selRole, st, guardMode ->
        val constraints = st.template.contrastConstraints
        val currentRoleAssignments = roles.mapKeys { it.key.code.uppercase() }
            .mapValues { (_, swatchIdx) -> pal.getOrElse(swatchIdx) { 0 } }

        val hasConstraintsForRole = constraints.any {
            it.foregroundRole.equals(selRole.code, ignoreCase = true) ||
            it.backgroundRole.equals(selRole.code, ignoreCase = true)
        }

        if (!hasConstraintsForRole || constraints.isEmpty()) {
            val allValid = (0 until minOf(16, pal.size)).associateWith { watch.facesapp.core.model.SwatchContrastStatus.VALID }
            return@combine ContrastEvaluation(allValid, false)
        }

        val validFlags = (0 until minOf(16, pal.size)).associateWith { idx ->
            val swatchColor = pal[idx]
            watch.facesapp.core.util.ColorContrast.isSwatchValidForRole(
                swatchColor = swatchColor,
                roleCode = selRole.code,
                currentAssignments = currentRoleAssignments,
                constraints = constraints
            )
        }

        // Smart relaxation rule:
        // If in HARD mode and NO swatch meets the constraint, relax to SOFT mode so user is never locked out
        val allInvalid = validFlags.values.none { it }
        val isRelaxed = guardMode == watch.facesapp.core.model.ContrastGuardMode.HARD && allInvalid
        val effectiveMode = if (isRelaxed) watch.facesapp.core.model.ContrastGuardMode.SOFT else guardMode

        val statuses = validFlags.mapValues { (_, isValid) ->
            if (isValid) {
                watch.facesapp.core.model.SwatchContrastStatus.VALID
            } else {
                if (effectiveMode == watch.facesapp.core.model.ContrastGuardMode.HARD) {
                    watch.facesapp.core.model.SwatchContrastStatus.DISABLED
                } else {
                    watch.facesapp.core.model.SwatchContrastStatus.WARNING
                }
            }
        }

        ContrastEvaluation(statuses, isRelaxed)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ContrastEvaluation(emptyMap(), false)
    )

    val swatchContrastStatuses: StateFlow<Map<Int, watch.facesapp.core.model.SwatchContrastStatus>> = contrastEvaluation
        .map { it.statuses }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val isConstraintRelaxed: StateFlow<Boolean> = contrastEvaluation
        .map { it.isRelaxed }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun toggleContrastGuardMode() {
        _contrastGuardMode.value = if (_contrastGuardMode.value == watch.facesapp.core.model.ContrastGuardMode.HARD) {
            watch.facesapp.core.model.ContrastGuardMode.SOFT
        } else {
            watch.facesapp.core.model.ContrastGuardMode.HARD
        }
    }

    val currentConfig: StateFlow<FaceConfig> = combine(_style, _paletteColors, _roleAssignments, _options, activeComplications) { st, pal, roles, opts, comps ->
        val resolvedColors = st.template.colorRoles.map { def ->
            val role = ColorRole.fromCode(def.code)
            val swatchIdx = roles[role]
            if (swatchIdx != null) {
                pal.getOrElse(swatchIdx) { def.defaultColor }
            } else {
                def.defaultColor
            }
        }
        val complicationsStr = comps.joinToString(",") { it.id }
        FaceConfig(
            id = UUID.randomUUID().toString(),
            style = st,
            colors = resolvedColors,
            options = opts + ("complications" to complicationsStr)
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
        _previewFaceMode.value = _previewFaceMode.value.next(complicationsRepo.activeComplications.value)
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
        if (_contrastGuardMode.value == watch.facesapp.core.model.ContrastGuardMode.HARD && !isConstraintRelaxed.value) {
            val status = swatchContrastStatuses.value[swatchIndex]
            if (status == watch.facesapp.core.model.SwatchContrastStatus.DISABLED) {
                return
            }
        }
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
        val pal = _paletteColors.value
        val constraints = _style.value.template.contrastConstraints
        val availableIndices = (0 until minOf(16, pal.size)).toList()
        if (availableIndices.isEmpty()) return

        var bestAssignment: Map<ColorRole, Int>? = null
        var bestScore = -1.0

        // Try up to 40 random permutations to pick one satisfying all contrast constraints
        for (attempt in 0 until 40) {
            val shuffled = availableIndices.shuffled()
            val candidate = roles.mapIndexed { idx, role ->
                role to shuffled.getOrElse(idx) { idx % availableIndices.size }
            }.toMap()

            val candidateColorMap = candidate.mapKeys { it.key.code.uppercase() }
                .mapValues { (_, sIdx) -> pal.getOrElse(sIdx) { 0 } }

            val satisfies = watch.facesapp.core.util.ColorContrast.satisfiesAllConstraints(candidateColorMap, constraints)
            val score = watch.facesapp.core.util.ColorContrast.calculateCumulativeScore(candidateColorMap, constraints)

            if (satisfies) {
                bestAssignment = candidate
                break
            }

            if (score > bestScore) {
                bestScore = score
                bestAssignment = candidate
            }
        }

        if (bestAssignment != null) {
            _roleAssignments.value = bestAssignment
            syncPreview()
        }
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
            faceDao.clearCurrentOnWatch()
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
