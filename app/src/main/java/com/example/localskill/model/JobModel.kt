package com.example.localskill.model

data class JobModel(
    val id: String = "",
    val employerId: String = "",
    val title: String = "",
    val description: String = "",
    val requiredSkills: String = "",
    val budget: String = "",
    val location: String = "",
    val area: String = "",
    val city: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val jobType: String = "",
    val status: String = JobStatus.OPEN.name,
    val deadline: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

enum class JobStatus {
    OPEN,
    CLOSED,
    COMPLETED
}
