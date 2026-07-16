package com.example.localskill.model

data class JobModel(
    val id: String = "",
    val companyId: String = "",
    val companyName: String = "",
    val companyLogoUrl: String = "",
    val companyVerified: Boolean = false,
    val title: String = "",
    val description: String = "",
    val responsibilities: List<String> = emptyList(),
    val requirements: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val categoryId: String = "",
    val categoryName: String = "",
    val location: String = "",
    val jobType: String = "",
    val workplaceType: String = "",
    val minimumSalary: Double? = null,
    val maximumSalary: Double? = null,
    val currency: String = "NPR",
    val experienceLevel: String = "",
    val educationRequirement: String = "",
    val vacancyCount: Int = 1,
    val applicationDeadline: Long = 0L,
    val status: String = JobStatus.ACTIVE.name,
    val applicationCount: Int = 0,
    val featured: Boolean = false,
    // Absent on jobs created before Phase 4; empty string is normalized to VISIBLE below
    // so existing rows keep showing up in discovery without a data migration.
    val moderationStatus: String = "",
    val moderationReason: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    val isExpired: Boolean
        get() = applicationDeadline in 1..<System.currentTimeMillis()

    val normalizedModerationStatus: String
        get() = moderationStatus.ifBlank { JobModerationStatus.VISIBLE.name }

    val isDiscoverable: Boolean
        get() = normalizedModerationStatus == JobModerationStatus.VISIBLE.name

    val isOpenForApplications: Boolean
        get() = status == JobStatus.ACTIVE.name && !isExpired && isDiscoverable
}
