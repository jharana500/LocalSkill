package com.example.localskill.services

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.localskill.utils.FileValidationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class FileMetadata(
    val displayName: String,
    val sizeBytes: Long,
    val mimeType: String?
)

/**
 * Reads content-Uri metadata via ContentResolver — kept out of both the
 * repository (which shouldn't need a Context) and composables (which
 * shouldn't validate files directly).
 */
class FileValidationService(private val context: Context) {

    // ContentResolver.query() for a content:// Uri can be a real, sometimes slow, IPC
    // call to another app's document provider (e.g. Drive) — never run it on Main.
    suspend fun readMetadata(uri: Uri): FileMetadata? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)
        var displayName = ""
        var sizeBytes = 0L

        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex >= 0) displayName = cursor.getString(nameIndex) ?: ""
                if (sizeIndex >= 0) sizeBytes = cursor.getLong(sizeIndex)
            }
        }

        if (displayName.isBlank()) return@withContext null
        FileMetadata(displayName = displayName, sizeBytes = sizeBytes, mimeType = mimeType)
    }

    fun validateResume(metadata: FileMetadata): String? =
        FileValidationUtils.validateResumeFile(metadata.mimeType, metadata.sizeBytes)

    fun validateImage(metadata: FileMetadata): String? =
        FileValidationUtils.validateImageFile(metadata.mimeType, metadata.sizeBytes)

    fun validateCompanyLogo(metadata: FileMetadata): String? =
        FileValidationUtils.validateCompanyLogo(metadata.mimeType, metadata.sizeBytes)

    fun validateCompanyDocument(metadata: FileMetadata): String? =
        FileValidationUtils.validateCompanyDocument(metadata.mimeType, metadata.sizeBytes)

    fun validateReportEvidence(metadata: FileMetadata): String? =
        FileValidationUtils.validateReportEvidence(metadata.mimeType, metadata.sizeBytes)
}
