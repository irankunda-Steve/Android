package com.audin.recorder.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RecordingItem(
    val name: String,
    val path: String,
    val createdAtMillis: Long,
    val durationMillis: Long,
    val sizeBytes: Long
)

class RecordingRepository(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var activeFile: File? = null

    private val recordingsDir: File by lazy {
        File(context.filesDir, "recordings").apply { mkdirs() }
    }

    fun start(): Result<Unit> {
        if (recorder != null) return Result.failure(IllegalStateException("Recorder already active"))
        return runCatching {
            val fileName = "audin_${timestamp()}.m4a"
            val outFile = File(recordingsDir, fileName)

            val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128_000)
                setAudioSamplingRate(44_100)
                setOutputFile(outFile.absolutePath)
                prepare()
                start()
            }

            recorder = mediaRecorder
            activeFile = outFile
        }
    }

    fun stop(): Result<RecordingItem?> {
        val mediaRecorder = recorder ?: return Result.success(null)
        return runCatching {
            mediaRecorder.stop()
            mediaRecorder.release()
            recorder = null

            val file = activeFile ?: return@runCatching null
            activeFile = null

            val duration = file.durationMillis()
            RecordingItem(
                name = file.name,
                path = file.absolutePath,
                createdAtMillis = file.lastModified(),
                durationMillis = duration,
                sizeBytes = file.length()
            )
        }.onFailure {
            recorder = null
            activeFile = null
        }
    }

    fun listRecordings(): List<RecordingItem> {
        return recordingsDir.listFiles().orEmpty()
            .filter { it.extension == "m4a" }
            .sortedByDescending { it.lastModified() }
            .map { file ->
                RecordingItem(
                    name = file.name,
                    path = file.absolutePath,
                    createdAtMillis = file.lastModified(),
                    durationMillis = file.durationMillis(),
                    sizeBytes = file.length()
                )
            }
    }

    fun delete(item: RecordingItem): Boolean = File(item.path).delete()

    fun rename(item: RecordingItem, newBaseName: String): RecordingItem? {
        val safeBase = newBaseName.trim().replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        if (safeBase.isBlank()) return null
        val target = File(recordingsDir, if (safeBase.endsWith(".m4a")) safeBase else "$safeBase.m4a")
        val source = File(item.path)
        if (!source.exists() || target.exists()) return null

        return if (source.renameTo(target)) {
            item.copy(
                name = target.name,
                path = target.absolutePath,
                createdAtMillis = target.lastModified(),
                sizeBytes = target.length(),
                durationMillis = target.durationMillis()
            )
        } else {
            null
        }
    }

    fun createShareIntent(item: RecordingItem): Intent {
        val file = File(item.path)
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "audio/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createPlayer(item: RecordingItem): MediaPlayer {
        return MediaPlayer().apply {
            setDataSource(item.path)
            prepare()
        }
    }

    fun exportToDownloads(item: RecordingItem): Result<Unit> {
        return runCatching {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, item.name)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Music/Audin")
            }
            val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Unable to create destination")

            resolver.openOutputStream(uri).use { out ->
                File(item.path).inputStream().use { input ->
                    input.copyTo(out ?: error("Unable to open destination"))
                }
            }
        }
    }

    private fun File.durationMillis(): Long {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(absolutePath)
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } finally {
            mmr.release()
        }
    }

    private fun timestamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    }
}
