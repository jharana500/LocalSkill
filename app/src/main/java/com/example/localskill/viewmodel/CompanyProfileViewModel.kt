package com.example.localskill.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.JobStatus
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.CompanyJobRepo
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

data class CompanyProfileUiState(
    val isLoading: Boolean = true,
    val company: CompanyModel = CompanyModel(),
    val postedJobCount: Int = 0,
    val activeJobCount: Int = 0,
    val isUploadingLogo: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed class CompanyProfileEvent {
    data class ShowMessage(val message: String) : CompanyProfileEvent()
}

class CompanyProfileViewModel(
    private val authRepo: AuthRepo,
    private val companyRepo: CompanyRepo,
    private val companyJobRepo: CompanyJobRepo,
    private val fileRepo: FileRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyProfileUiState())
    val uiState: StateFlow<CompanyProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<CompanyProfileEvent>(Channel.BUFFERED)
    val events: Flow<CompanyProfileEvent> = _events.receiveAsFlow()

    fun loadProfile() {
        val companyId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val companyResult = companyRepo.getCompany(companyId)
            val jobsResult = companyJobRepo.getCompanyJobs(companyId)
            if (companyResult is ResultState.Success) {
                val jobs = (jobsResult as? ResultState.Success)?.data.orEmpty()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    company = companyResult.data,
                    postedJobCount = jobs.size,
                    activeJobCount = jobs.count { it.status == JobStatus.ACTIVE.name }
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = (companyResult as? ResultState.Error)?.message ?: "Unable to load your company profile."
                )
            }
        }
    }

    fun updateProfile(
        companyName: String,
        contactPersonName: String,
        phone: String,
        website: String,
        description: String,
        industry: String,
        employeeCountRange: String,
        registrationNumber: String,
        panNumber: String,
        address: String,
        city: String,
        district: String
    ) {
        if (_uiState.value.isSaving) return
        val current = _uiState.value.company

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val updated = current.copy(
                companyName = companyName,
                contactPersonName = contactPersonName,
                phone = phone,
                website = website,
                description = description,
                industry = industry,
                employeeCountRange = employeeCountRange,
                registrationNumber = registrationNumber,
                panNumber = panNumber,
                address = address,
                city = city,
                district = district
            )
            when (val result = companyRepo.updateCompanyProfile(updated)) {
                is ResultState.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    _events.send(CompanyProfileEvent.ShowMessage("Profile updated."))
                    loadProfile()
                }

                is ResultState.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    _events.send(CompanyProfileEvent.ShowMessage(result.message))
                }

                else -> Unit
            }
        }
    }

    fun uploadLogo(uri: Uri) {
        val companyId = authRepo.currentUserId() ?: return
        if (_uiState.value.isUploadingLogo) return
        val previousLogoUrl = _uiState.value.company.logoUrl

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingLogo = true)
            when (val result = fileRepo.uploadCompanyLogo(companyId, uri)) {
                is ResultState.Success -> {
                    companyRepo.updateLogoUrl(companyId, result.data)
                    if (previousLogoUrl.isNotBlank()) fileRepo.deleteCompanyLogo(previousLogoUrl)
                    _uiState.value = _uiState.value.copy(isUploadingLogo = false)
                    _events.send(CompanyProfileEvent.ShowMessage("Logo updated."))
                    loadProfile()
                }

                is ResultState.Error -> {
                    _uiState.value = _uiState.value.copy(isUploadingLogo = false)
                    _events.send(CompanyProfileEvent.ShowMessage(result.message))
                }

                else -> Unit
            }
        }
    }
}
