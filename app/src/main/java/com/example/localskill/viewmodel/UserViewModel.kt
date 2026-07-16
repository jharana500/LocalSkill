package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.UserModel
import com.example.localskill.repo.UserRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserUiState(
    val isLoading: Boolean = false,
    val user: UserModel? = null,
    val users: List<UserModel> = emptyList(),
    val errorMessage: String? = null
)

class UserViewModel(private val repo: UserRepo) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    fun getUserById(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repo.getUserById(userId)) {
                is ResultState.Success -> _uiState.value =
                    _uiState.value.copy(isLoading = false, user = result.data)

                is ResultState.Error -> _uiState.value =
                    _uiState.value.copy(isLoading = false, errorMessage = result.message)

                else -> Unit
            }
        }
    }

    fun getAllUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repo.getAllUsers()) {
                is ResultState.Success -> _uiState.value =
                    _uiState.value.copy(isLoading = false, users = result.data)

                is ResultState.Error -> _uiState.value =
                    _uiState.value.copy(isLoading = false, errorMessage = result.message)

                else -> Unit
            }
        }
    }

    fun updateUser(user: UserModel) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repo.updateUser(user)) {
                is ResultState.Success -> _uiState.value =
                    _uiState.value.copy(isLoading = false, user = user)

                is ResultState.Error -> _uiState.value =
                    _uiState.value.copy(isLoading = false, errorMessage = result.message)

                else -> Unit
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repo.deleteUser(userId)) {
                is ResultState.Success -> _uiState.value =
                    _uiState.value.copy(isLoading = false, user = null)

                is ResultState.Error -> _uiState.value =
                    _uiState.value.copy(isLoading = false, errorMessage = result.message)

                else -> Unit
            }
        }
    }

}
