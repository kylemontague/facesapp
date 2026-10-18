package watch.facesapp.mobile.ui.home

import android.app.Application
import android.content.Context
import android.text.format.DateUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.core.model.FaceStyle
import watch.facesapp.mobile.data.db.FaceEntity
import watch.facesapp.mobile.data.db.FaceWearLogEntity
import watch.facesapp.mobile.data.db.FaceWearSession
import watch.facesapp.mobile.data.db.FacesDatabase
import watch.facesapp.mobile.data.prefs.ComplicationsRepository
import watch.facesapp.mobile.data.sync.WearSyncManager
import watch.facesapp.mobile.data.weather.WeatherManager
import watch.facesapp.mobile.data.calendar.CalendarManager
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

data class FaceStats(
    val faceId: String,
    val wearCount: Int,
    val totalDurationMs: Long,
    val isMostWorn: Boolean = false,
    val formattedDuration: String = ""
)

fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> {
            val remHours = hours % 24
            if (remHours > 0) "${days}d ${remHours}h" else "${days}d"
        }
        hours > 0 -> {
            val remMins = minutes % 60
            if (remMins > 0) "${hours}h ${remMins}m" else "${hours}h"
        }
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FacesDatabase.getInstance(application)
    private val faceDao = db.faceDao()
    private val syncManager = WearSyncManager(application)
    private val complicationsRepo = ComplicationsRepository.getInstance(application)
    private val weatherManager = WeatherManager(application)
    private val calendarManager = CalendarManager(application)

    val activeComplications: StateFlow<List<FaceMode>> = complicationsRepo.activeComplications

    private val _isAmbient = MutableStateFlow(false)
    val isAmbient: StateFlow<Boolean> = _isAmbient.asStateFlow()

    private val _calendar = MutableStateFlow(Calendar.getInstance())
    val calendar: StateFlow<Calendar> = _calendar.asStateFlow()

    private val _faceMode = MutableStateFlow(FaceMode.TIME)
    val faceMode: StateFlow<FaceMode> = _faceMode.asStateFlow()

    private var phoneHealthManager: watch.facesapp.mobile.sensor.PhoneHealthManager? = null

    private val _complicationState = MutableStateFlow(
        run {
            val initialSteps = application.getSharedPreferences("faces_phone_health_prefs", Context.MODE_PRIVATE)
                .getLong("today_steps", 1420L)
            val initialCalories = (initialSteps * 0.04f).toInt()
            val initialDistance = initialSteps * 0.000762f
            val initialActiveMins = (initialSteps / 100).toInt()
            watch.facesapp.core.model.ComplicationState(
                batteryPercent = getBatteryPercentage(application),
                stepCount = initialSteps,
                caloriesBurned = initialCalories,
                distanceKm = initialDistance,
                activeMinutes = initialActiveMins
            )
        }
    )
    val complicationState: StateFlow<watch.facesapp.core.model.ComplicationState> = _complicationState.asStateFlow()

    private val _wearStatus = MutableStateFlow<watch.facesapp.mobile.data.sync.WearConnectionStatus>(
        watch.facesapp.mobile.data.sync.WearConnectionStatus.NotConnected()
    )
    val wearStatus: StateFlow<watch.facesapp.mobile.data.sync.WearConnectionStatus> = _wearStatus.asStateFlow()

    private val _isCheckingWearStatus = MutableStateFlow(false)
    val isCheckingWearStatus: StateFlow<Boolean> = _isCheckingWearStatus.asStateFlow()

    private val _showWearStatusOverlay = MutableStateFlow(false)
    val showWearStatusOverlay: StateFlow<Boolean> = _showWearStatusOverlay.asStateFlow()

    private var hasDismissedOverlayThisSession = false

    fun checkWearStatus(showOverlayIfInactive: Boolean = true) {
        viewModelScope.launch {
            _isCheckingWearStatus.value = true
            val status = syncManager.checkWearStatus()
            _wearStatus.value = status
            _isCheckingWearStatus.value = false
            if (status is watch.facesapp.mobile.data.sync.WearConnectionStatus.ConnectedAndActive) {
                _showWearStatusOverlay.value = false
                refreshLiveComplications()
            } else if (showOverlayIfInactive && !hasDismissedOverlayThisSession) {
                _showWearStatusOverlay.value = true
            }
        }
    }

    fun refreshLiveComplications() {
        viewModelScope.launch {
            try {
                val weather = weatherManager.fetchCurrentWeather()
                val event = calendarManager.getNextEvent()
                if (weather != null) {
                    _complicationState.value = _complicationState.value.copy(
                        weatherTempC = weather.tempC,
                        weatherCondition = weather.condition
                    )
                    syncManager.syncWeather(weather.tempC, weather.condition)
                }
                if (event != null) {
                    _complicationState.value = _complicationState.value.copy(
                        nextEventTitle = event.title,
                        nextEventMinutes = event.minutesUntil
                    )
                    syncManager.syncCalendarEvent(event.title, event.minutesUntil)
                }

                // Synchronize health state to watch as well
                val currentState = _complicationState.value
                val steps = currentState.stepCount ?: 1420L
                val cals = currentState.caloriesBurned ?: (steps * 0.04f).toInt()
                val dist = currentState.distanceKm ?: (steps * 0.000762f)
                val activeMins = currentState.activeMinutes ?: (steps / 100).toInt()
                syncManager.syncHealth(steps, currentState.heartRateBpm, cals, dist, activeMins)
            } catch (_: Exception) {
            }
        }
    }

    fun dismissWearStatusOverlay() {
        hasDismissedOverlayThisSession = true
        _showWearStatusOverlay.value = false
    }

    fun openWearStatusOverlay() {
        _showWearStatusOverlay.value = true
        checkWearStatus(showOverlayIfInactive = true)
    }

    init {
        // 1 Hz live ticking clock
        viewModelScope.launch {
            while (isActive) {
                _calendar.value = Calendar.getInstance()
                delay((if (_isAmbient.value) 10_000L else 1000L).milliseconds)
            }
        }

        // Live Weather & Calendar fetch and Wear OS synchronization
        refreshLiveComplications()

        // Live Health & Activity tracking on Phone
        phoneHealthManager = watch.facesapp.mobile.sensor.PhoneHealthManager(application) { healthState ->
            _complicationState.value = _complicationState.value.copy(
                stepCount = healthState.stepCount,
                heartRateBpm = healthState.heartRateBpm ?: _complicationState.value.heartRateBpm,
                caloriesBurned = healthState.caloriesBurned,
                distanceKm = healthState.distanceKm,
                activeMinutes = healthState.activeMinutes
            )
            viewModelScope.launch {
                syncManager.syncHealth(
                    healthState.stepCount ?: 0L,
                    healthState.heartRateBpm,
                    healthState.caloriesBurned,
                    healthState.distanceKm,
                    healthState.activeMinutes
                )
            }
        }.apply {
            start()
        }

        // Live Health Stream from Wear OS watch (when user is wearing the watch)
        syncManager.setHealthUpdateListener { steps, bpm, calories, distance, activeMins ->
            phoneHealthManager?.updateFromWatch(steps, bpm, calories, distance, activeMins)
            _complicationState.value = _complicationState.value.copy(
                stepCount = steps,
                heartRateBpm = if (bpm != null && bpm > 0) bpm else _complicationState.value.heartRateBpm,
                caloriesBurned = calories ?: (steps * 0.04f).toInt(),
                distanceKm = distance ?: (steps * 0.000762f),
                activeMinutes = activeMins ?: (steps / 100).toInt()
            )
        }

        // Database auto-seed safeguard (in case database was empty or after destructive migration)
        viewModelScope.launch(Dispatchers.IO) {
            if (faceDao.getCount() == 0) {
                val presets = FaceEntity.createStockPresets()
                faceDao.insertFaces(presets)
                presets.firstOrNull { it.isCurrentOnWatch }?.let { activeFace ->
                    faceDao.recordWearFace(activeFace.id, activeFace.lastWornAt ?: System.currentTimeMillis())
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        phoneHealthManager?.stop()
        syncManager.removeHealthUpdateListener()
    }

    // Periodic ticker for relative time displays (every 30 seconds instead of 1 Hz)
    private val periodicTicker: Flow<Long> = kotlinx.coroutines.flow.flow {
        while (kotlinx.coroutines.currentCoroutineContext().isActive) {
            emit(System.currentTimeMillis())
            delay(30_000L)
        }
    }

    data class PagerStateData(
        val items: List<HomePagerItem> = emptyList(),
        val activeIndex: Int = 0,
        val activeFaceId: String? = null
    )

    val pagerStateData: StateFlow<PagerStateData> = combine(
        faceDao.getAllFaces(),
        faceDao.getAllWearSessions(),
        periodicTicker
    ) { faces, wearSessions, now ->
        buildPagerItems(faces, wearSessions, now)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = buildPagerItems(FaceEntity.createStockPresets(), emptyList(), System.currentTimeMillis())
    )

    private fun buildPagerItems(
        faces: List<FaceEntity>,
        wearSessions: List<FaceWearSession>,
        now: Long
    ): PagerStateData {
        val effectiveFaces = faces.ifEmpty { FaceEntity.createStockPresets() }

        // Active face on user's wrist
        val activeEntity = effectiveFaces.firstOrNull { it.isCurrentOnWatch }
            ?: effectiveFaces.maxByOrNull { it.lastWornAt ?: 0L }
            ?: effectiveFaces.first()

        // Active wear log session (the most recent session for this active face that hasn't ended)
        val activeSession = wearSessions.firstOrNull { it.face?.id == activeEntity.id && it.log.wornUntil == null }
            ?: wearSessions.firstOrNull { it.face?.id == activeEntity.id }

        // Past wear sessions: all wear logs excluding the currently active session
        val pastSessions = wearSessions.filter { session ->
            session.face != null && (activeSession == null || session.log.logId != activeSession.log.logId)
        }

        // Reversing pastSessions (which was in DESC order by wornAt) places the oldest at index 0 (far left)
        // and the most recently worn session immediately adjacent to the active face (swiping left).
        val historicalItems = pastSessions.reversed().map { session ->
            val relativeTime = DateUtils.getRelativeTimeSpanString(
                session.log.wornAt,
                now,
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
            HomePagerItem.HistoricalFace(
                logId = session.log.logId,
                entity = session.face!!,
                wornAt = session.log.wornAt,
                title = relativeTime
            )
        }

        val activeItem = HomePagerItem.ActiveFace(
            entity = activeEntity,
            title = "On yer wrist!"
        )

        // Base templates ready to customize (swiping right)
        val templateItems = FaceStyle.entries.map { style ->
            HomePagerItem.TemplateFace(style = style)
        }

        val allItems = historicalItems + activeItem + templateItems
        val activeIndex = historicalItems.size

        return PagerStateData(
            items = allItems,
            activeIndex = activeIndex,
            activeFaceId = activeEntity.id
        )
    }

    fun getFaceStats(faceId: String): Flow<FaceStats> = faceDao.getAllWearLogs()
        .combine(periodicTicker) { allLogs, now ->
            val faceLogs = allLogs.filter { it.faceId == faceId }
            val wearCount = faceLogs.size
            val totalDuration = faceLogs.sumOf { log ->
                val end = log.wornUntil ?: now
                (end - log.wornAt).coerceAtLeast(0L)
            }
            val maxWearCount = allLogs.groupBy { it.faceId }.maxOfOrNull { it.value.size } ?: 0
            val isMostWorn = wearCount > 0 && wearCount == maxWearCount

            FaceStats(
                faceId = faceId,
                wearCount = wearCount,
                totalDurationMs = totalDuration,
                isMostWorn = isMostWorn,
                formattedDuration = formatDuration(totalDuration)
            )
        }

    fun previewPagerFace(config: FaceConfig) {
        viewModelScope.launch {
            val configWithComplications = config.withComplications(complicationsRepo.activeComplications.value)
            syncManager.sendPreviewUpdate(configWithComplications)
        }
    }

    fun wearAgain(entity: FaceEntity) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            faceDao.clearCurrentOnWatch()
            val updated = entity.copy(lastWornAt = now, isCurrentOnWatch = true)
            faceDao.insertFace(updated)
            faceDao.recordWearFace(entity.id, now)
            val configWithUniversalComplications = updated.toFaceConfig().withComplications(complicationsRepo.activeComplications.value)
            syncManager.syncCurrentFace(configWithUniversalComplications)
        }
    }

    fun toggleComplication(mode: FaceMode) {
        complicationsRepo.toggleComplication(mode)
        syncActiveFaceComplications()
    }

    fun updateComplicationsOrder(newOrder: List<FaceMode>) {
        complicationsRepo.setComplications(newOrder)
        syncActiveFaceComplications()
    }

    private fun syncActiveFaceComplications() {
        viewModelScope.launch {
            val currentFace = faceDao.getCurrentFace() ?: return@launch
            val updatedConfig = currentFace.toFaceConfig().withComplications(complicationsRepo.activeComplications.value)
            val entity = FaceEntity.fromFaceConfig(
                config = updatedConfig,
                name = currentFace.name,
                lastWornAt = currentFace.lastWornAt,
                isCurrentOnWatch = true
            )
            faceDao.insertFace(entity)
            syncManager.syncCurrentFace(updatedConfig)
        }
    }

    fun toggleAmbientMode() {
        _isAmbient.value = !_isAmbient.value
    }

    private var complicationRevertJob: Job? = null

    fun cycleFaceMode(activeComplications: List<FaceMode> = complicationsRepo.activeComplications.value) {
        _faceMode.value = _faceMode.value.nextAvailable(activeComplications, _complicationState.value)
        complicationRevertJob?.cancel()
        if (_faceMode.value != FaceMode.TIME) {
            complicationRevertJob = viewModelScope.launch {
                delay(5000L)
                _faceMode.value = FaceMode.TIME
            }
        }
    }

    fun toggleFaceMode() {
        cycleFaceMode(complicationsRepo.activeComplications.value)
    }

    private fun getBatteryPercentage(context: android.content.Context): Int {
        return try {
            val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) (level * 100) / scale else 85
        } catch (e: Exception) {
            85
        }
    }

    fun toggleFavorite(faceId: String, currentFavorite: Boolean) {
        viewModelScope.launch {
            faceDao.setFavorite(faceId, !currentFavorite)
        }
    }

    fun deleteFace(faceId: String) {
        viewModelScope.launch {
            faceDao.deleteFaceById(faceId)
        }
    }

    fun getWearLogsForFace(faceId: String): Flow<List<FaceWearLogEntity>> =
        faceDao.getWearLogsForFace(faceId)

    fun importSharedFace(uri: android.net.Uri, onImported: ((FaceEntity) -> Unit)? = null) {
        val sharedData = watch.facesapp.core.util.FaceShareUtils.parseShareUri(uri) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val entity = FaceEntity(
                id = java.util.UUID.randomUUID().toString(),
                style = sharedData.style,
                colors = sharedData.colors,
                name = sharedData.name,
                options = sharedData.options,
                isFavorite = false,
                createdAt = System.currentTimeMillis(),
                lastWornAt = System.currentTimeMillis(),
                isCurrentOnWatch = true
            )
            faceDao.insertFace(entity)
            faceDao.recordWearFace(entity.id, entity.lastWornAt ?: System.currentTimeMillis())
            val configWithComplications = entity.toFaceConfig().withComplications(complicationsRepo.activeComplications.value)
            syncManager.syncCurrentFace(configWithComplications)
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                onImported?.invoke(entity)
            }
        }
    }
}
