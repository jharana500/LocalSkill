package com.example.localskill.repo

import com.example.localskill.model.EducationModel
import com.example.localskill.model.ExperienceModel
import com.example.localskill.model.JobSeekerProfileModel
import com.example.localskill.model.ResumeModel
import com.example.localskill.model.SkillModel
import com.example.localskill.utils.ResultState

interface JobSeekerProfileRepo {

    /** Loads the profile at jobSeekerProfiles/{userId}, creating a default one on first access. */
    suspend fun getProfile(userId: String): ResultState<JobSeekerProfileModel>

    suspend fun updatePersonalInfo(
        userId: String,
        headline: String,
        bio: String,
        city: String,
        district: String
    ): ResultState<Unit>

    suspend fun addEducation(userId: String, education: EducationModel): ResultState<Unit>

    suspend fun updateEducation(userId: String, education: EducationModel): ResultState<Unit>

    suspend fun removeEducation(userId: String, educationId: String): ResultState<Unit>

    suspend fun addExperience(userId: String, experience: ExperienceModel): ResultState<Unit>

    suspend fun updateExperience(userId: String, experience: ExperienceModel): ResultState<Unit>

    suspend fun removeExperience(userId: String, experienceId: String): ResultState<Unit>

    suspend fun addSkill(userId: String, skill: SkillModel): ResultState<Unit>

    suspend fun removeSkill(userId: String, skillName: String): ResultState<Unit>

    suspend fun updateResume(userId: String, resume: ResumeModel): ResultState<Unit>

    suspend fun updateProfileImageUrl(userId: String, imageUrl: String): ResultState<Unit>

    suspend fun updateProfileVisibility(userId: String, visibility: String): ResultState<Unit>
}
