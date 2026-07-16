package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.AccountStatus
import com.example.localskill.model.UserModel
import com.example.localskill.model.UserRole
import com.example.localskill.repo.AdminRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class AdminUsersUiState(
    val isLoading: Boolean = true,
    val users: List<UserModel> = emptyList(),
    val searchQuery: String = "",
    val roleFilter: UserRole? = null,
    val statusFilter: AccountStatus? = null,
    val currentAdminId: String = "",
    val errorMessage: String? = null
) {
    val filtered: List<UserModel>
        get() = users
            .filter { roleFilter == null || it.role == roleFilter.name }
            .filter { statusFilter == null || it.accountStatus == statusFilter.name }
            .filter {
                searchQuery.isBlank() ||
                    it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
            }
}

sealed class AdminUserEvent {
    data class ShowMessage(val message: String) : AdminUserEvent()
}

class AdminUserViewModel(
    private val authRepo: AuthRepo,
    private val adminRepo: AdminRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUsersUiState())
    val uiState: StateFlow<AdminUsersUiState> = _uiState.asStateFlow()

    private val _events = Channel<AdminUserEvent>(Channel.BUFFERED)
    val events: Flow<AdminUserEvent> = _events.receiveAsFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = adminRepo.getAllUsers()) {
                is ResultState.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    users = result.data,
                    currentAdminId = authRepo.currentUserId().orEmpty()
                )

                is ResultState.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                else -> Unit
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setRoleFilter(role: UserRole?) {
        _uiState.value = _uiState.value.copy(roleFilter = role)
    }

    fun setStatusFilter(status: AccountStatus?) {
        _uiState.value = _uiState.value.copy(statusFilter = status)
    }

    fun suspendUser(targetUserId: String) {
        val adminId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            when (val result = adminRepo.suspendUser(adminId, targetUserId)) {
                is ResultState.Success -> {
                    _events.send(AdminUserEvent.ShowMessage("User suspended."))
                    loadUsers()
                }

                is ResultState.Error -> _events.send(AdminUserEvent.ShowMessage(result.message))
                else -> Unit
            }
        }
    }

    fun reactivateUser(targetUserId: String) {
        val adminId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            when (val result = adminRepo.reactivateUser(adminId, targetUserId)) {
                is ResultState.Success -> {
                    _events.send(AdminUserEvent.ShowMessage("User reactivated."))
                    loadUsers()
                }

                is ResultState.Error -> _events.send(AdminUserEvent.ShowMessage(result.message))
                else -> Unit
            }
        }
    }
}
