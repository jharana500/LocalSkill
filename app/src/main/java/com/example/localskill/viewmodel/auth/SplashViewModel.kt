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

data class SplashUiState(
    val isLoading: Boolean = true,
    val routeToRole: UserRole? = null,
    val needsLogin: Boolean = false,
    val needsRole: Boolean = false
)

class SplashViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(SplashUiState())
        private set

    fun checkSession() {
        val userId = authRepository.currentUserId()
        if (userId == null) {
            uiState = SplashUiState(isLoading = false, needsLogin = true)
            return
        }
        userRepository.getUser(userId) { result ->
            uiState = when (result) {
                Resource.Loading -> SplashUiState(isLoading = true)
                is Resource.Error -> SplashUiState(isLoading = false, needsLogin = true)
                is Resource.Success -> {
                    val role = result.data.role
                    if (role == null) SplashUiState(isLoading = false, needsRole = true)
                    else SplashUiState(isLoading = false, routeToRole = role)
                }
            }
        }
    }
}
