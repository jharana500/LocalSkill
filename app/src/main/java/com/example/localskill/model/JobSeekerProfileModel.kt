package com.example.localskill.model

enum class ProfileVisibility {
    PUBLIC,
    PRIVATE
}

/**
 * Detailed Job Seeker profile stored at jobSeekerProfiles/{userId}. Account-
 * level fields (email, role, accountStatus) stay on UserModel and are not
 * duplicated here.
 */
data class JobSeekerProfileModel(
    val userId: String = "",
    val headline: String = "",
    val bio: String = "",
    val city: String = "",
    val district: String = "",
    val preferredJobTypes: List<String> = emptyList(),
    val preferredLocations: List<String> = emptyList(),
    val skills: List<SkillModel> = emptyList(),
    val education: List<EducationModel> = emptyList(),
    val experience: List<ExperienceModel> = emptyList(),
    val resume: ResumeModel = ResumeModel(),
    val profileImageUrl: String = "",
    val profileVisibility: String = ProfileVisibility.PUBLIC.name,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
