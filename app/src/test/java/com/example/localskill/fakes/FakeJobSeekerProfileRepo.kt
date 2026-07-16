package com.example.localskill.fakes

import com.example.localskill.model.EducationModel
import com.example.localskill.model.ExperienceModel
import com.example.localskill.model.JobSeekerProfileModel
import com.example.localskill.model.ResumeModel
import com.example.localskill.model.SkillModel
import com.example.localskill.repo.JobSeekerProfileRepo
import com.example.localskill.utils.ResultState
import java.util.UUID

class FakeJobSeekerProfileRepo : JobSeekerProfileRepo {

    val profiles = mutableMapOf<String, JobSeekerProfileModel>()

    private fun profileFor(userId: String) = profiles.getOrPut(userId) { JobSeekerProfileModel(userId = userId) }

    override suspend fun getProfile(userId: String): ResultState<JobSeekerProfileModel> =
        ResultState.Success(profileFor(userId))

    override suspend fun updatePersonalInfo(
        userId: String,
        headline: String,
        bio: String,
        city: String,
        district: String
    ): ResultState<Unit> {
        profiles[userId] = profileFor(userId).copy(headline = headline, bio = bio, city = city, district = district)
        return ResultState.Success(Unit)
    }

    override suspend fun addEducation(userId: String, education: EducationModel): ResultState<Unit> {
        val entry = education.copy(id = education.id.ifBlank { UUID.randomUUID().toString() })
        val profile = profileFor(userId)
        profiles[userId] = profile.copy(education = profile.education + entry)
        return ResultState.Success(Unit)
    }

    override suspend fun updateEducation(userId: String, education: EducationModel): ResultState<Unit> {
        val profile = profileFor(userId)
        profiles[userId] = profile.copy(
            education = profile.education.map { if (it.id == education.id) education else it }
        )
        return ResultState.Success(Unit)
    }

    override suspend fun removeEducation(userId: String, educationId: String): ResultState<Unit> {
        val profile = profileFor(userId)
        profiles[userId] = profile.copy(education = profile.education.filterNot { it.id == educationId })
        return ResultState.Success(Unit)
    }

    override suspend fun addExperience(userId: String, experience: ExperienceModel): ResultState<Unit> {
        val entry = experience.copy(id = experience.id.ifBlank { UUID.randomUUID().toString() })
        val profile = profileFor(userId)
        profiles[userId] = profile.copy(experience = profile.experience + entry)
        return ResultState.Success(Unit)
    }

    override suspend fun updateExperience(userId: String, experience: ExperienceModel): ResultState<Unit> {
        val profile = profileFor(userId)
        profiles[userId] = profile.copy(
            experience = profile.experience.map { if (it.id == experience.id) experience else it }
        )
        return ResultState.Success(Unit)
    }

    override suspend fun removeExperience(userId: String, experienceId: String): ResultState<Unit> {
        val profile = profileFor(userId)
        profiles[userId] = profile.copy(experience = profile.experience.filterNot { it.id == experienceId })
        return ResultState.Success(Unit)
    }

    override suspend fun addSkill(userId: String, skill: SkillModel): ResultState<Unit> {
        val profile = profileFor(userId)
        if (profile.skills.any { it.normalizedName == skill.normalizedName }) {
            return ResultState.Error("This skill has already been added.")
        }
        profiles[userId] = profile.copy(skills = profile.skills + skill)
        return ResultState.Success(Unit)
    }

    override suspend fun removeSkill(userId: String, skillName: String): ResultState<Unit> {
        val normalized = skillName.trim().lowercase()
        val profile = profileFor(userId)
        profiles[userId] = profile.copy(skills = profile.skills.filterNot { it.normalizedName == normalized })
        return ResultState.Success(Unit)
    }

    override suspend fun updateResume(userId: String, resume: ResumeModel): ResultState<Unit> {
        profiles[userId] = profileFor(userId).copy(resume = resume)
        return ResultState.Success(Unit)
    }

    override suspend fun updateProfileImageUrl(userId: String, imageUrl: String): ResultState<Unit> {
        profiles[userId] = profileFor(userId).copy(profileImageUrl = imageUrl)
        return ResultState.Success(Unit)
    }

    override suspend fun updateProfileVisibility(userId: String, visibility: String): ResultState<Unit> {
        profiles[userId] = profileFor(userId).copy(profileVisibility = visibility)
        return ResultState.Success(Unit)
    }
}
