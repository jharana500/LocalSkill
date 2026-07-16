package com.example.localskill.services

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.localskill.utils.FileValidationUtils

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

    fun readMetadata(uri: Uri): FileMetadata? {
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

        if (displayName.isBlank()) return null
        return FileMetadata(displayName = displayName, sizeBytes = sizeBytes, mimeType = mimeType)
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
