package com.example.localskill.viewmodel.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.UserModel
import com.example.localskill.repo.auth.AuthRepository
import com.example.localskill.repo.auth.AuthRepositoryImpl
import com.example.localskill.repo.user.UserRepository
import com.example.localskill.repo.user.UserRepositoryImpl
import com.example.localskill.utils.FcmTokenUtils
import com.example.localskill.utils.Resource
import com.example.localskill.utils.Validators

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val user: UserModel? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null)
    }

    fun login() {
        val error = Validators.email(uiState.email) ?: Validators.password(uiState.password)
        if (error != null) {
            uiState = uiState.copy(errorMessage = error)
            return
        }
        authRepository.login(uiState.email, uiState.password) { result ->
            when (result) {
                Resource.Loading -> uiState = uiState.copy(isLoading = true, errorMessage = null)
                is Resource.Error -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> {
                    FcmTokenUtils.saveTokenForUser(result.data)
                    loadUser(result.data)
                }
            }
        }
    }

    private fun loadUser(userId: String) {
        userRepository.getUser(userId) { result ->
            when (result) {
                Resource.Loading -> uiState = uiState.copy(isLoading = true)
                is Resource.Error -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> uiState = uiState.copy(isLoading = false, user = result.data)
            }
        }
    }
}
