package com.example.localskill.model

data class ApplicationModel(
    val id: String = "",
    val jobId: String = "",
    val workerId: String = "",
    val employerId: String = "",
    val message: String = "",
    val status: String = ApplicationStatus.PENDING.name,
    val jobTitle: String = "",
    val jobLocation: String = "",
    val jobBudget: String = "",
    val workerName: String = "",
    val workerPhone: String = "",
    val workerLocation: String = "",
    val appliedAt: Long = 0L,
    val updatedAt: Long = 0L
)

enum class ApplicationStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
