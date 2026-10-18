package com.faces.mobile.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.faces.core.model.FaceConfig
import com.faces.mobile.data.db.FaceEntity
import com.faces.mobile.data.db.FacesDatabase
import com.faces.mobile.data.sync.WearSyncManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FacesDatabase.getInstance(application)
    private val faceDao = db.faceDao()
    private val syncManager = WearSyncManager(application)

    val faces: StateFlow<List<FaceEntity>> = faceDao.getAllFaces()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FaceEntity.createStockPresets()
        )

    private val _isAmbient = MutableStateFlow(false)
    val isAmbient: StateFlow<Boolean> = _isAmbient.asStateFlow()

    private val _calendar = MutableStateFlow(Calendar.getInstance())
    val calendar: StateFlow<Calendar> = _calendar.asStateFlow()

    private val _faceMode = MutableStateFlow(com.faces.core.model.FaceMode.TIME)
    val faceMode: StateFlow<com.faces.core.model.FaceMode> = _faceMode.asStateFlow()

    init {
        // Live 1 Hz ticking clock
        viewModelScope.launch {
            while (isActive) {
                _calendar.value = Calendar.getInstance()
                delay(if (_isAmbient.value) 10_000L else 1000L)
            }
        }
    }

    fun toggleAmbientMode() {
        _isAmbient.value = !_isAmbient.value
    }

    fun toggleFaceMode() {
        _faceMode.value = when (_faceMode.value) {
            com.faces.core.model.FaceMode.TIME -> com.faces.core.model.FaceMode.DATE
            com.faces.core.model.FaceMode.DATE -> com.faces.core.model.FaceMode.TIME
            com.faces.core.model.FaceMode.STEPS -> com.faces.core.model.FaceMode.TIME
        }
    }

    fun toggleFavorite(face: FaceEntity) {
        viewModelScope.launch {
            faceDao.setFavorite(face.id, !face.isFavorite)
        }
    }

    fun deleteFace(face: FaceEntity) {
        viewModelScope.launch {
            faceDao.deleteFace(face)
        }
    }

    fun syncToWatch(config: FaceConfig) {
        viewModelScope.launch {
            syncManager.syncCurrentFace(config)
        }
    }
}
