package com.example.localskill.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.EducationModel
import com.example.localskill.model.ExperienceModel
import com.example.localskill.model.JobSeekerProfileModel
import com.example.localskill.model.ResumeModel
import com.example.localskill.model.SkillModel
import com.example.localskill.repo.ApplicationRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.FileRepo
import com.example.localskill.repo.JobSeekerProfileRepo
import com.example.localskill.repo.SavedJobRepo
import com.example.localskill.repo.UserRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class JobSeekerProfileUiState(
    val isLoading: Boolean = true,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val profile: JobSeekerProfileModel = JobSeekerProfileModel(),
    val applicationCount: Int = 0,
    val savedJobCount: Int = 0,
    val isMutating: Boolean = false,
    val errorMessage: String? = null
)

sealed class ProfileEvent {
    data class ShowMessage(val message: String) : ProfileEvent()
}

class JobSeekerProfileViewModel(
    private val authRepo: AuthRepo,
    private val userRepo: UserRepo,
    private val jobSeekerProfileRepo: JobSeekerProfileRepo,
    private val applicationRepo: ApplicationRepo,
    private val savedJobRepo: SavedJobRepo,
    private val fileRepo: FileRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobSeekerProfileUiState())
    val uiState: StateFlow<JobSeekerProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    private fun currentUserId(): String? = authRepo.currentUserId()

    fun loadProfile() {
        val userId = currentUserId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val userResult = userRepo.getUserById(userId)
            val profileResult = jobSeekerProfileRepo.getProfile(userId)
            val applicationsResult = applicationRepo.getUserApplications(userId)
            val savedResult = savedJobRepo.getSavedJobIds(userId)

            val user = (userResult as? ResultState.Success)?.data
            val profile = (profileResult as? ResultState.Success)?.data
                ?: JobSeekerProfileModel(userId = userId)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isMutating = false,
                fullName = user?.fullName.orEmpty(),
                email = user?.email.orEmpty(),
                phone = user?.phone.orEmpty(),
                profile = profile,
                applicationCount = (applicationsResult as? ResultState.Success)?.data?.size ?: 0,
                savedJobCount = (savedResult as? ResultState.Success)?.data?.size ?: 0,
                errorMessage = (profileResult as? ResultState.Error)?.message
            )
        }
    }

    private fun runMutation(successMessage: String? = null, operation: suspend (String) -> ResultState<Unit>) {
        val userId = currentUserId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMutating = true)
            when (val result = operation(userId)) {
                is ResultState.Success -> {
                    loadProfile()
                    if (successMessage != null) _events.send(ProfileEvent.ShowMessage(successMessage))
                }

                is ResultState.Error -> {
                    _uiState.value = _uiState.value.copy(isMutating = false)
                    _events.send(ProfileEvent.ShowMessage(result.message))
                }

                else -> Unit
            }
        }
    }

    fun updatePersonalInfo(headline: String, bio: String, city: String, district: String) =
        runMutation("Profile updated") { userId ->
            jobSeekerProfileRepo.updatePersonalInfo(userId, headline, bio, city, district)
        }

    fun addEducation(education: EducationModel) =
        runMutation { userId -> jobSeekerProfileRepo.addEducation(userId, education) }

    fun updateEducation(education: EducationModel) =
        runMutation { userId -> jobSeekerProfileRepo.updateEducation(userId, education) }

    fun removeEducation(educationId: String) =
        runMutation { userId -> jobSeekerProfileRepo.removeEducation(userId, educationId) }

    fun addExperience(experience: ExperienceModel) =
        runMutation { userId -> jobSeekerProfileRepo.addExperience(userId, experience) }

    fun updateExperience(experience: ExperienceModel) =
        runMutation { userId -> jobSeekerProfileRepo.updateExperience(userId, experience) }

    fun removeExperience(experienceId: String) =
        runMutation { userId -> jobSeekerProfileRepo.removeExperience(userId, experienceId) }

    fun addSkill(skill: SkillModel) =
        runMutation { userId -> jobSeekerProfileRepo.addSkill(userId, skill) }

    fun removeSkill(skillName: String) =
        runMutation { userId -> jobSeekerProfileRepo.removeSkill(userId, skillName) }

    fun updateProfileVisibility(visibility: String) =
        runMutation { userId -> jobSeekerProfileRepo.updateProfileVisibility(userId, visibility) }

    fun uploadResume(uri: Uri) {
        val userId = currentUserId() ?: return
        val oldResumeUrl = _uiState.value.profile.resume.downloadUrl
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMutating = true)
            when (val uploadResult = fileRepo.uploadResume(userId, uri)) {
                is ResultState.Success -> finishFileMutation(
                    saveResult = jobSeekerProfileRepo.updateResume(userId, uploadResult.data),
                    oldUrlToDelete = oldResumeUrl,
                    deleteOld = { fileRepo.deleteResume(it) },
                    successMessage = "Resume updated"
                )

                is ResultState.Error -> {
                    _uiState.value = _uiState.value.copy(isMutating = false)
                    _events.send(ProfileEvent.ShowMessage(uploadResult.message))
                }

                else -> Unit
            }
        }
    }

    fun removeResume() {
        val oldResumeUrl = _uiState.value.profile.resume.downloadUrl
        runMutation("Resume removed") { userId ->
            if (oldResumeUrl.isNotBlank()) fileRepo.deleteResume(oldResumeUrl)
            jobSeekerProfileRepo.updateResume(userId, ResumeModel())
        }
    }

    fun uploadProfileImage(uri: Uri) {
        val userId = currentUserId() ?: return
        val oldImageUrl = _uiState.value.profile.profileImageUrl
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMutating = true)
            when (val uploadResult = fileRepo.uploadProfileImage(userId, uri)) {
                is ResultState.Success -> finishFileMutation(
                    saveResult = jobSeekerProfileRepo.updateProfileImageUrl(userId, uploadResult.data),
                    oldUrlToDelete = oldImageUrl,
                    deleteOld = { fileRepo.deleteProfileImage(it) },
                    successMessage = "Profile photo updated"
                )

                is ResultState.Error -> {
                    _uiState.value = _uiState.value.copy(isMutating = false)
                    _events.send(ProfileEvent.ShowMessage(uploadResult.message))
                }

                else -> Unit
            }
        }
    }

    fun removeProfileImage() {
        val oldImageUrl = _uiState.value.profile.profileImageUrl
        runMutation { userId ->
            if (oldImageUrl.isNotBlank()) fileRepo.deleteProfileImage(oldImageUrl)
            jobSeekerProfileRepo.updateProfileImageUrl(userId, "")
        }
    }

    private suspend fun finishFileMutation(
        saveResult: ResultState<Unit>,
        oldUrlToDelete: String,
        deleteOld: suspend (String) -> ResultState<Unit>,
        successMessage: String
    ) {
        if (saveResult is ResultState.Success) {
            if (oldUrlToDelete.isNotBlank()) deleteOld(oldUrlToDelete)
            loadProfile()
            _events.send(ProfileEvent.ShowMessage(successMessage))
        } else {
            _uiState.value = _uiState.value.copy(isMutating = false)
            _events.send(ProfileEvent.ShowMessage((saveResult as? ResultState.Error)?.message ?: "Unable to save changes"))
        }
    }
}
