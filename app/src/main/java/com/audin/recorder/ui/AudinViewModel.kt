package com.audin.recorder.ui

import android.content.Intent
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.audin.recorder.data.RecordingItem
import com.audin.recorder.data.RecordingRepository

data class AudinUiState(
    val hasAudioPermission: Boolean = false,
    val isRecording: Boolean = false,
    val playingPath: String? = null,
    val recordings: List<RecordingItem> = emptyList(),
    val filteredRecordings: List<RecordingItem> = emptyList(),
    val query: String = "",
    val message: String? = null
)

class AudinViewModel(private val repository: RecordingRepository) : ViewModel() {

    var uiState by mutableStateOf(
        AudinUiState(recordings = repository.listRecordings(), filteredRecordings = repository.listRecordings())
    )
        private set

    private var player: MediaPlayer? = null

    fun onPermissionResult(granted: Boolean) {
        uiState = uiState.copy(hasAudioPermission = granted)
    }

    fun startRecording() {
        val result = repository.start()
        uiState = if (result.isSuccess) {
            uiState.copy(isRecording = true, message = "Recording started")
        } else {
            uiState.copy(message = result.exceptionOrNull()?.message ?: "Unable to record")
        }
    }

    fun stopRecording() {
        if (!uiState.isRecording) return
        val result = repository.stop()
        val latest = result.getOrNull()
        refresh()
        uiState = uiState.copy(
            isRecording = false,
            message = latest?.let { "Saved ${it.name}" } ?: "Recording stopped"
        )
    }

    fun play(item: RecordingItem) {
        stopPlayback()
        runCatching {
            repository.createPlayer(item).apply {
                setOnCompletionListener {
                    stopPlayback()
                }
                start()
            }
        }.onSuccess {
            player = it
            uiState = uiState.copy(playingPath = item.path, message = "Playing ${item.name}")
        }.onFailure {
            uiState = uiState.copy(message = it.message ?: "Unable to play recording")
        }
    }

    fun stopPlayback() {
        player?.runCatching {
            if (isPlaying) stop()
            release()
        }
        player = null
        uiState = uiState.copy(playingPath = null)
    }

    fun deleteRecording(item: RecordingItem) {
        if (repository.delete(item)) {
            if (uiState.playingPath == item.path) stopPlayback()
            refresh()
            uiState = uiState.copy(message = "Deleted ${item.name}")
        }
    }

    fun renameRecording(item: RecordingItem, newName: String) {
        val renamed = repository.rename(item, newName)
        refresh()
        uiState = uiState.copy(message = if (renamed != null) "Renamed to ${renamed.name}" else "Rename failed")
    }

    fun shareRecording(item: RecordingItem): Intent {
        return Intent.createChooser(repository.createShareIntent(item), "Share recording")
    }

    fun search(query: String) {
        val filtered = uiState.recordings.filter {
            it.name.contains(query, ignoreCase = true)
        }
        uiState = uiState.copy(query = query, filteredRecordings = filtered)
    }

    fun export(item: RecordingItem) {
        val success = repository.exportToDownloads(item).isSuccess
        uiState = uiState.copy(message = if (success) "Exported to Music/Audin" else "Export failed")
    }

    private fun refresh() {
        val all = repository.listRecordings()
        val filtered = all.filter { it.name.contains(uiState.query, ignoreCase = true) }
        uiState = uiState.copy(recordings = all, filteredRecordings = filtered)
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}

class AudinViewModelFactory(private val repository: RecordingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudinViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudinViewModel(repository) as T
        }
        error("Unknown ViewModel class")
    }
}
