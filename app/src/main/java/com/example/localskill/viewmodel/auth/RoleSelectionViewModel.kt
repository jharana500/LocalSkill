package com.example.localskill.viewmodel.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.UserRole
import com.example.localskill.repo.auth.AuthRepository
import com.example.localskill.repo.auth.AuthRepositoryImpl
import com.example.localskill.repo.user.UserRepository
import com.example.localskill.repo.user.UserRepositoryImpl
import com.example.localskill.utils.Resource

data class RoleSelectionUiState(
    val selectedRole: UserRole? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val savedRole: UserRole? = null
)

class RoleSelectionViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(RoleSelectionUiState())
        private set

    fun selectRole(role: UserRole) {
        uiState = uiState.copy(selectedRole = role, errorMessage = null)
    }

    fun saveRole() {
        val role = uiState.selectedRole
        val userId = authRepository.currentUserId()
        if (role == null) {
            uiState = uiState.copy(errorMessage = "Choose Worker or Employer")
            return
        }
        if (userId == null) {
            uiState = uiState.copy(errorMessage = "Session expired. Please login again")
            return
        }
        userRepository.updateRole(userId, role) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true, errorMessage = null)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> uiState.copy(isLoading = false, savedRole = role)
            }
        }
    }
}
