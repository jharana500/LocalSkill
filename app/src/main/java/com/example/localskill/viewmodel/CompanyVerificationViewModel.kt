package com.example.localskill.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.CompanyDocumentModel
import com.example.localskill.model.CompanyModel
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.CompanyRepo
import com.example.localskill.repo.FileRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class CompanyVerificationUiState(
    val isLoading: Boolean = true,
    val company: CompanyModel = CompanyModel(),
    val documents: List<CompanyDocumentModel> = emptyList(),
    val uploadingDocumentType: String? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null
)

sealed class CompanyVerificationEvent {
    data class ShowMessage(val message: String) : CompanyVerificationEvent()
}

class CompanyVerificationViewModel(
    private val authRepo: AuthRepo,
    private val companyRepo: CompanyRepo,
    private val fileRepo: FileRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyVerificationUiState())
    val uiState: StateFlow<CompanyVerificationUiState> = _uiState.asStateFlow()

    private val _events = Channel<CompanyVerificationEvent>(Channel.BUFFERED)
    val events: Flow<CompanyVerificationEvent> = _events.receiveAsFlow()

    fun loadVerification() {
        val companyId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val companyResult = companyRepo.getCompany(companyId)
            val documentsResult = companyRepo.getDocuments(companyId)
            if (companyResult is ResultState.Success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    company = companyResult.data,
                    documents = (documentsResult as? ResultState.Success)?.data ?: emptyList()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = (companyResult as? ResultState.Error)?.message ?: "Unable to load verification status."
                )
            }
        }
    }

    fun uploadDocument(documentType: String, uri: Uri) {
        val companyId = authRepo.currentUserId() ?: return
        if (_uiState.value.uploadingDocumentType != null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(uploadingDocumentType = documentType)
            when (val uploadResult = fileRepo.uploadCompanyDocument(companyId, documentType, uri)) {
                is ResultState.Success -> {
                    val existing = _uiState.value.documents.firstOrNull { it.documentType == documentType }
                    companyRepo.saveDocumentMetadata(uploadResult.data)
                    if (existing != null) {
                        companyRepo.deleteDocumentMetadata(companyId, existing.id)
                        fileRepo.deleteCompanyDocument(existing.downloadUrl)
                    }
                    _uiState.value = _uiState.value.copy(uploadingDocumentType = null)
                    _events.send(CompanyVerificationEvent.ShowMessage("Document uploaded."))
                    loadVerification()
                }

                is ResultState.Error -> {
                    _uiState.value = _uiState.value.copy(uploadingDocumentType = null)
                    _events.send(CompanyVerificationEvent.ShowMessage(uploadResult.message))
                }

                else -> Unit
            }
        }
    }

    fun removeDocument(document: CompanyDocumentModel) {
        val companyId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            companyRepo.deleteDocumentMetadata(companyId, document.id)
            fileRepo.deleteCompanyDocument(document.downloadUrl)
            loadVerification()
        }
    }

    fun submitVerification() {
        val companyId = authRepo.currentUserId() ?: return
        if (_uiState.value.isSubmitting) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            when (val result = companyRepo.submitVerification(companyId)) {
                is ResultState.Success -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitSuccess = true)
                    _events.send(CompanyVerificationEvent.ShowMessage("Submitted for verification."))
                    loadVerification()
                }

                is ResultState.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = result.message
                )

                else -> Unit
            }
        }
    }
}
