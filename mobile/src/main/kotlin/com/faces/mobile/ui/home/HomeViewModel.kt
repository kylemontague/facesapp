package com.faces.mobile.ui.home

import android.app.Application
import android.text.format.DateUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.faces.core.model.FaceConfig
import com.faces.core.model.FaceMode
import com.faces.core.model.FaceStyle
import com.faces.mobile.data.db.FaceEntity
import com.faces.mobile.data.db.FaceWearLogEntity
import com.faces.mobile.data.db.FacesDatabase
import com.faces.mobile.data.sync.WearSyncManager
import com.faces.mobile.data.db.FaceWearSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

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

    private val _isAmbient = MutableStateFlow(false)
    val isAmbient: StateFlow<Boolean> = _isAmbient.asStateFlow()

    private val _calendar = MutableStateFlow(Calendar.getInstance())
    val calendar: StateFlow<Calendar> = _calendar.asStateFlow()

    private val _faceMode = MutableStateFlow(FaceMode.TIME)
    val faceMode: StateFlow<FaceMode> = _faceMode.asStateFlow()

    init {
        // 1 Hz live ticking clock
        viewModelScope.launch {
            while (isActive) {
                _calendar.value = Calendar.getInstance()
                delay(if (_isAmbient.value) 10_000L else 1000L)
            }
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

    data class PagerStateData(
        val items: List<HomePagerItem> = emptyList(),
        val activeIndex: Int = 0,
        val activeFaceId: String? = null
    )

    val pagerStateData: StateFlow<PagerStateData> = combine(
        faceDao.getAllFaces(),
        faceDao.getAllWearSessions(),
        _calendar
    ) { faces, wearSessions, nowCal ->
        val now = nowCal.timeInMillis
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
        val effectiveFaces = if (faces.isEmpty()) FaceEntity.createStockPresets() else faces

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
        .combine(_calendar) { allLogs, cal ->
            val now = cal.timeInMillis
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

    fun wearAgain(entity: FaceEntity) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            faceDao.recordWearFace(entity.id, now)
            syncManager.syncCurrentFace(entity.toFaceConfig())
        }
    }

    fun toggleAmbientMode() {
        _isAmbient.value = !_isAmbient.value
    }

    fun toggleFaceMode() {
        _faceMode.value = when (_faceMode.value) {
            FaceMode.TIME -> FaceMode.DATE
            FaceMode.DATE -> FaceMode.TIME
            FaceMode.STEPS -> FaceMode.TIME
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
}
