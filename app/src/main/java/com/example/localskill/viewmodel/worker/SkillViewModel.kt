package com.example.localskill.viewmodel.worker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.SkillModel
import com.example.localskill.repo.auth.AuthRepository
import com.example.localskill.repo.auth.AuthRepositoryImpl
import com.example.localskill.repo.skill.SkillRepository
import com.example.localskill.repo.skill.SkillRepositoryImpl
import com.example.localskill.utils.Resource
import com.example.localskill.utils.Validators

data class SkillUiState(
    val title: String = "",
    val category: String = "",
    val rate: String = "",
    val experience: String = "",
    val location: String = "",
    val availability: String = "",
    val description: String = "",
    val portfolioUrl: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSaved: Boolean = false
)

class SkillViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val skillRepository: SkillRepository = SkillRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(SkillUiState())
        private set

    fun update(field: String, value: String) {
        uiState = when (field) {
            "title" -> uiState.copy(title = value, errorMessage = null)
            "category" -> uiState.copy(category = value, errorMessage = null)
            "rate" -> uiState.copy(rate = value, errorMessage = null)
            "experience" -> uiState.copy(experience = value, errorMessage = null)
            "location" -> uiState.copy(location = value, errorMessage = null)
            "availability" -> uiState.copy(availability = value, errorMessage = null)
            "portfolioUrl" -> uiState.copy(portfolioUrl = value, errorMessage = null)
            else -> uiState.copy(description = value, errorMessage = null)
        }
    }

    fun saveSkill() {
        val error = Validators.required(uiState.title, "Skill title")
            ?: Validators.required(uiState.category, "Category")
            ?: Validators.required(uiState.rate, "Rate")
            ?: Validators.required(uiState.experience, "Experience")
            ?: Validators.required(uiState.location, "Location")
            ?: Validators.required(uiState.availability, "Availability")
            ?: Validators.required(uiState.description, "Description")
            ?: Validators.optionalUrl(uiState.portfolioUrl, "portfolio URL")
        if (error != null) {
            uiState = uiState.copy(errorMessage = error)
            return
        }
        val workerId = authRepository.currentUserId()
        if (workerId == null) {
            uiState = uiState.copy(errorMessage = "Please login again")
            return
        }
        val skill = SkillModel(
            workerId = workerId,
            title = uiState.title.trim(),
            category = uiState.category.trim(),
            rate = uiState.rate.trim(),
            experience = uiState.experience.trim(),
            location = uiState.location.trim(),
            availability = uiState.availability.trim(),
            description = uiState.description.trim(),
            portfolioUrl = uiState.portfolioUrl.trim()
        )
        skillRepository.addSkill(skill) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true, errorMessage = null)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> SkillUiState(
                    successMessage = "Skill saved successfully",
                    isSaved = true
                )
            }
        }
    }
}
