package com.example.localskill.model

data class SavedJobModel(
    val jobId: String = "",
    val userId: String = "",
    val savedAt: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "jobId" to jobId,
        "userId" to userId,
        "savedAt" to savedAt
    )
}
