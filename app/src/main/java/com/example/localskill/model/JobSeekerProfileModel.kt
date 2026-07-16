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
) {
    /**
     * Single deterministic completion calculation, used everywhere a
     * completion percentage is shown so screens can never disagree.
     */
    val completionPercentage: Int
        get() {
            val checks = listOf(
                headline.isNotBlank(),
                bio.isNotBlank(),
                city.isNotBlank(),
                skills.isNotEmpty(),
                education.isNotEmpty(),
                experience.isNotEmpty(),
                resume.isPresent,
                profileImageUrl.isNotBlank()
            )
            val completed = checks.count { it }
            return (completed * 100) / checks.size
        }

    val missingSections: List<String>
        get() = buildList {
            if (headline.isBlank() || bio.isBlank() || city.isBlank()) add("Personal information")
            if (skills.isEmpty()) add("Skills")
            if (education.isEmpty()) add("Education")
            if (experience.isEmpty()) add("Experience")
            if (!resume.isPresent) add("Resume")
            if (profileImageUrl.isBlank()) add("Profile photo")
        }
}
