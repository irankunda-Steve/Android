package com.audin.recorder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.audin.recorder.data.RecordingRepository
import com.audin.recorder.ui.AudinApp
import com.audin.recorder.ui.AudinViewModel
import com.audin.recorder.ui.AudinViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: AudinViewModel by viewModels {
        AudinViewModelFactory(RecordingRepository(this))
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onPermissionResult(hasPermission)

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                AudinApp(
                    state = viewModel.uiState,
                    onStartRecording = {
                        if (viewModel.uiState.hasAudioPermission) {
                            viewModel.startRecording()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onStopRecording = viewModel::stopRecording,
                    onPlay = viewModel::play,
                    onStopPlayback = viewModel::stopPlayback,
                    onDelete = viewModel::deleteRecording,
                    onRename = viewModel::renameRecording,
                    onShare = { item -> startActivity(viewModel.shareRecording(item)) },
                    onExport = viewModel::export,
                    onSearch = viewModel::search
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopPlayback()
        viewModel.stopRecording()
    }
}
