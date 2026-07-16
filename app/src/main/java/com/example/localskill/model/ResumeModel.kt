package com.example.localskill.model

data class ResumeModel(
    val fileName: String = "",
    val downloadUrl: String = "",
    val fileSizeBytes: Long = 0L,
    val uploadedAt: Long = 0L
) {
    val isPresent: Boolean
        get() = downloadUrl.isNotBlank()
}
