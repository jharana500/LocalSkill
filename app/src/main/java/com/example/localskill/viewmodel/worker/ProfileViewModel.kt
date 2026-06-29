package com.example.localskill.viewmodel.worker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.SkillModel
import com.example.localskill.model.UserModel
import com.example.localskill.model.UserRole
import com.example.localskill.model.ReviewModel
import com.example.localskill.repo.auth.AuthRepository
import com.example.localskill.repo.auth.AuthRepositoryImpl
import com.example.localskill.repo.skill.SkillRepository
import com.example.localskill.repo.skill.SkillRepositoryImpl
import com.example.localskill.repo.review.ReviewRepository
import com.example.localskill.repo.review.ReviewRepositoryImpl
import com.example.localskill.repo.user.UserRepository
import com.example.localskill.repo.user.UserRepositoryImpl
import com.example.localskill.utils.Resource
import com.example.localskill.utils.Validators

data class ProfileUiState(
    val user: UserModel? = null,
    val skills: List<SkillModel> = emptyList(),
    val reviews: List<ReviewModel> = emptyList(),
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val area: String = "",
    val city: String = "",
    val bio: String = "",
    val experience: String = "",
    val availability: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val skillRepository: SkillRepository = SkillRepositoryImpl(),
    private val reviewRepository: ReviewRepository = ReviewRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(ProfileUiState())
        private set

    fun load() {
        val userId = authRepository.currentUserId()
        if (userId == null) {
            uiState = uiState.copy(errorMessage = "Session expired. Please login again")
            return
        }
        userRepository.getUser(userId) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> result.data.toProfileState(uiState.skills)
            }
        }
        skillRepository.getSkillsByWorker(userId) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> uiState.copy(isLoading = false, skills = result.data)
            }
        }
        reviewRepository.getReviewsForUser(userId) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> uiState.copy(isLoading = false, reviews = result.data)
            }
        }
    }

    fun update(field: String, value: String) {
        uiState = when (field) {
            "fullName" -> uiState.copy(fullName = value, errorMessage = null, successMessage = null)
            "phone" -> uiState.copy(phone = value, errorMessage = null, successMessage = null)
            "location" -> uiState.copy(location = value, errorMessage = null, successMessage = null)
            "area" -> uiState.copy(area = value, errorMessage = null, successMessage = null)
            "city" -> uiState.copy(city = value, errorMessage = null, successMessage = null)
            "bio" -> uiState.copy(bio = value, errorMessage = null, successMessage = null)
            "experience" -> uiState.copy(experience = value, errorMessage = null, successMessage = null)
            "availability" -> uiState.copy(availability = value, errorMessage = null, successMessage = null)
            else -> uiState
        }
    }

    fun saveProfile() {
        val current = uiState.user
        if (current == null) {
            uiState = uiState.copy(errorMessage = "Profile is still loading")
            return
        }

        val error = Validators.required(uiState.fullName, "Full name")
            ?: Validators.required(uiState.phone, "Phone")
        if (error != null) {
            uiState = uiState.copy(errorMessage = error)
            return
        }

        val updated = current.copy(
            fullName = uiState.fullName.trim(),
            phone = uiState.phone.trim(),
            location = uiState.location.trim(),
            area = uiState.area.trim(),
            city = uiState.city.trim(),
            locationUpdatedAt = System.currentTimeMillis(),
            bio = uiState.bio.trim(),
            experience = uiState.experience.trim(),
            availability = uiState.availability.trim(),
            role = current.role ?: UserRole.WORKER
        )
        userRepository.updateProfile(updated) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isSaving = true, errorMessage = null, successMessage = null)
                is Resource.Error -> uiState.copy(isSaving = false, errorMessage = result.message)
                is Resource.Success -> updated.toProfileState(uiState.skills).copy(
                    reviews = uiState.reviews,
                    successMessage = "Profile updated"
                )
            }
        }
    }

    fun logout() = authRepository.logout()
}

private fun UserModel.toProfileState(skills: List<SkillModel>): ProfileUiState =
    ProfileUiState(
        user = this,
        skills = skills,
        fullName = fullName,
        email = email,
        phone = phone,
        location = location,
        area = area,
        city = city,
        bio = bio,
        experience = experience,
        availability = availability,
        isLoading = false
    )
