package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.JobCategoryModel
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

data class AdminCategoriesUiState(
    val isLoading: Boolean = true,
    val categories: List<JobCategoryModel> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed class AdminCategoryEvent {
    data class ShowMessage(val message: String) : AdminCategoryEvent()
}

class AdminCategoryViewModel(
    private val authRepo: AuthRepo,
    private val adminRepo: AdminRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCategoriesUiState())
    val uiState: StateFlow<AdminCategoriesUiState> = _uiState.asStateFlow()

    private val _events = Channel<AdminCategoryEvent>(Channel.BUFFERED)
    val events: Flow<AdminCategoryEvent> = _events.receiveAsFlow()

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = adminRepo.getCategories(includeInactive = true)) {
                is ResultState.Success -> _uiState.value = _uiState.value.copy(isLoading = false, categories = result.data)
                is ResultState.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                else -> Unit
            }
        }
    }

    fun addCategory(name: String) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = adminRepo.addCategory(name)) {
                is ResultState.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    _events.send(AdminCategoryEvent.ShowMessage("Category added."))
                    loadCategories()
                }

                is ResultState.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    _events.send(AdminCategoryEvent.ShowMessage(result.message))
                }

                else -> Unit
            }
        }
    }

    fun setCategoryActive(categoryId: String, isActive: Boolean) {
        val adminId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            when (val result = adminRepo.setCategoryActive(categoryId, isActive, adminId)) {
                is ResultState.Success -> loadCategories()
                is ResultState.Error -> _events.send(AdminCategoryEvent.ShowMessage(result.message))
                else -> Unit
            }
        }
    }
}
