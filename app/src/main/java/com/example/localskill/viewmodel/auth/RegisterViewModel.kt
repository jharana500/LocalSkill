package com.example.localskill.viewmodel.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.UserModel
import com.example.localskill.repo.auth.AuthRepository
import com.example.localskill.repo.auth.AuthRepositoryImpl
import com.example.localskill.utils.FcmTokenUtils
import com.example.localskill.utils.Resource
import com.example.localskill.utils.Validators

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registeredUserId: String? = null
)

class RegisterViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun onFullNameChange(value: String) { uiState = uiState.copy(fullName = value, errorMessage = null) }
    fun onEmailChange(value: String) { uiState = uiState.copy(email = value, errorMessage = null) }
    fun onPhoneChange(value: String) { uiState = uiState.copy(phone = value, errorMessage = null) }
    fun onPasswordChange(value: String) { uiState = uiState.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChange(value: String) { uiState = uiState.copy(confirmPassword = value, errorMessage = null) }

    fun register() {
        val error = Validators.required(uiState.fullName, "Full name")
            ?: Validators.email(uiState.email)
            ?: Validators.required(uiState.phone, "Phone")
            ?: Validators.password(uiState.password)
            ?: Validators.confirmPassword(uiState.password, uiState.confirmPassword)
        if (error != null) {
            uiState = uiState.copy(errorMessage = error)
            return
        }
        val user = UserModel(
            fullName = uiState.fullName.trim(),
            email = uiState.email.trim(),
            phone = uiState.phone.trim()
        )
        authRepository.register(user, uiState.password) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true, errorMessage = null)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> {
                    FcmTokenUtils.saveTokenForUser(result.data)
                    uiState.copy(isLoading = false, registeredUserId = result.data)
                }
            }
        }
    }
}
