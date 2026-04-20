package com.audin.recorder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.audin.recorder.data.RecordingItem
import java.text.DateFormat
import java.util.Date

@Composable
fun AudinApp(
    state: AudinUiState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPlay: (RecordingItem) -> Unit,
    onStopPlayback: () -> Unit,
    onDelete: (RecordingItem) -> Unit,
    onRename: (RecordingItem, String) -> Unit,
    onShare: (RecordingItem) -> Unit,
    onExport: (RecordingItem) -> Unit,
    onSearch: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Audin Voice Recorder", style = MaterialTheme.typography.headlineSmall)
        Text(state.message.orEmpty(), style = MaterialTheme.typography.bodySmall)

        OutlinedTextField(
            value = state.query,
            onValueChange = onSearch,
            label = { Text("Search recordings") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.isRecording) {
                Button(onClick = onStopRecording) {
                    Text("Stop")
                }
            } else {
                Button(onClick = onStartRecording) {
                    Text("Record")
                }
            }
            if (state.playingPath != null) {
                Button(onClick = onStopPlayback) {
                    Text("Stop Playback")
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.filteredRecordings, key = { it.path }) { item ->
                RecordingRow(
                    item = item,
                    isPlaying = state.playingPath == item.path,
                    onPlay = { onPlay(item) },
                    onDelete = { onDelete(item) },
                    onRename = { newName -> onRename(item, newName) },
                    onShare = { onShare(item) },
                    onExport = { onExport(item) }
                )
            }
        }
    }
}

@Composable
private fun RecordingRow(
    item: RecordingItem,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit
) {
    var renameText by remember(item.path) { mutableStateOf(item.name.removeSuffix(".m4a")) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${formatDuration(item.durationMillis)} • ${formatSize(item.sizeBytes)} • ${formatTime(item.createdAtMillis)}",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = renameText,
                onValueChange = { renameText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rename") }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onPlay) { Text(if (isPlaying) "Re-play" else "Play") }
                Button(onClick = { onRename(renameText) }) { Text("Save Name") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onShare) { Text("Share") }
                Button(onClick = onExport) { Text("Export") }
                Button(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

private fun formatDuration(durationMillis: Long): String {
    val seconds = durationMillis / 1000
    return "%d:%02d".format(seconds / 60, seconds % 60)
}

private fun formatSize(sizeBytes: Long): String {
    val kb = sizeBytes / 1024.0
    return if (kb < 1024) "%.1f KB".format(kb) else "%.1f MB".format(kb / 1024)
}

private fun formatTime(createdAtMillis: Long): String {
    return DateFormat.getDateTimeInstance().format(Date(createdAtMillis))
}
