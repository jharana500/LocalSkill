package com.example.localskill.repo

import com.example.localskill.model.EducationModel
import com.example.localskill.model.ExperienceModel
import com.example.localskill.model.JobSeekerProfileModel
import com.example.localskill.model.ResumeModel
import com.example.localskill.model.SkillModel
import com.example.localskill.utils.Constants
import com.example.localskill.utils.FirebaseErrorMapper
import com.example.localskill.utils.ResultState
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class JobSeekerProfileRepoImpl(
    database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : JobSeekerProfileRepo {

    private val profilesRef: DatabaseReference = database.getReference(Constants.JOB_SEEKER_PROFILES_NODE)

    override suspend fun getProfile(userId: String): ResultState<JobSeekerProfileModel> = try {
        val snapshot = profilesRef.child(userId).get().await()
        val existing = snapshot.getValue(JobSeekerProfileModel::class.java)
        if (existing != null) {
            ResultState.Success(existing)
        } else {
            val now = System.currentTimeMillis()
            val defaultProfile = JobSeekerProfileModel(userId = userId, createdAt = now, updatedAt = now)
            profilesRef.child(userId).setValue(defaultProfile).await()
            ResultState.Success(defaultProfile)
        }
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    private suspend fun saveProfile(profile: JobSeekerProfileModel): ResultState<Unit> = try {
        val updated = profile.copy(updatedAt = System.currentTimeMillis())
        profilesRef.child(profile.userId).setValue(updated).await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    private suspend fun withProfile(
        userId: String,
        mutate: (JobSeekerProfileModel) -> ResultState<JobSeekerProfileModel>
    ): ResultState<Unit> {
        val current = getProfile(userId)
        if (current !is ResultState.Success) {
            return ResultState.Error((current as? ResultState.Error)?.message ?: "Unable to load profile")
        }
        return when (val mutated = mutate(current.data)) {
            is ResultState.Success -> saveProfile(mutated.data)
            is ResultState.Error -> ResultState.Error(mutated.message, mutated.throwable)
            else -> ResultState.Error("Unable to update profile")
        }
    }

    override suspend fun updatePersonalInfo(
        userId: String,
        headline: String,
        bio: String,
        city: String,
        district: String
    ): ResultState<Unit> = withProfile(userId) { profile ->
        ResultState.Success(profile.copy(headline = headline, bio = bio, city = city, district = district))
    }

    override suspend fun addEducation(userId: String, education: EducationModel): ResultState<Unit> =
        withProfile(userId) { profile ->
            val entry = education.copy(id = education.id.ifBlank { UUID.randomUUID().toString() })
            ResultState.Success(profile.copy(education = profile.education + entry))
        }

    override suspend fun updateEducation(userId: String, education: EducationModel): ResultState<Unit> =
        withProfile(userId) { profile ->
            ResultState.Success(
                profile.copy(education = profile.education.map { if (it.id == education.id) education else it })
            )
        }

    override suspend fun removeEducation(userId: String, educationId: String): ResultState<Unit> =
        withProfile(userId) { profile ->
            ResultState.Success(profile.copy(education = profile.education.filterNot { it.id == educationId }))
        }

    override suspend fun addExperience(userId: String, experience: ExperienceModel): ResultState<Unit> =
        withProfile(userId) { profile ->
            val entry = experience.copy(id = experience.id.ifBlank { UUID.randomUUID().toString() })
            ResultState.Success(profile.copy(experience = profile.experience + entry))
        }

    override suspend fun updateExperience(userId: String, experience: ExperienceModel): ResultState<Unit> =
        withProfile(userId) { profile ->
            ResultState.Success(
                profile.copy(experience = profile.experience.map { if (it.id == experience.id) experience else it })
            )
        }

    override suspend fun removeExperience(userId: String, experienceId: String): ResultState<Unit> =
        withProfile(userId) { profile ->
            ResultState.Success(profile.copy(experience = profile.experience.filterNot { it.id == experienceId }))
        }

    override suspend fun addSkill(userId: String, skill: SkillModel): ResultState<Unit> =
        withProfile(userId) { profile ->
            val alreadyExists = profile.skills.any { it.normalizedName == skill.normalizedName }
            if (alreadyExists) {
                ResultState.Error("This skill has already been added.")
            } else {
                ResultState.Success(profile.copy(skills = profile.skills + skill))
            }
        }

    override suspend fun removeSkill(userId: String, skillName: String): ResultState<Unit> =
        withProfile(userId) { profile ->
            val normalized = skillName.trim().lowercase()
            ResultState.Success(profile.copy(skills = profile.skills.filterNot { it.normalizedName == normalized }))
        }

    override suspend fun updateResume(userId: String, resume: ResumeModel): ResultState<Unit> =
        withProfile(userId) { profile -> ResultState.Success(profile.copy(resume = resume)) }

    override suspend fun updateProfileImageUrl(userId: String, imageUrl: String): ResultState<Unit> =
        withProfile(userId) { profile -> ResultState.Success(profile.copy(profileImageUrl = imageUrl)) }

    override suspend fun updateProfileVisibility(userId: String, visibility: String): ResultState<Unit> =
        withProfile(userId) { profile -> ResultState.Success(profile.copy(profileVisibility = visibility)) }
}
