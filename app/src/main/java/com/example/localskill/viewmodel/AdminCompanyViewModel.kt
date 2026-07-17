package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.CompanyDocumentModel
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.repo.AdminRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.CompanyRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

enum class AdminCompanyFilterTab {
    ALL, PENDING, VERIFIED, REJECTED, DRAFT;

    fun matches(status: String): Boolean = when (this) {
        ALL -> true
        PENDING -> status == CompanyVerificationStatus.PENDING.name
        VERIFIED -> status == CompanyVerificationStatus.VERIFIED.name
        REJECTED -> status == CompanyVerificationStatus.REJECTED.name
        DRAFT -> status == CompanyVerificationStatus.DRAFT.name
    }
}

data class AdminCompaniesUiState(
    val isLoading: Boolean = true,
    val companies: List<CompanyModel> = emptyList(),
    val filterTab: AdminCompanyFilterTab = AdminCompanyFilterTab.PENDING,
    val processingCompanyId: String? = null,
    val errorMessage: String? = null
) {
    val filtered: List<CompanyModel> get() = companies.filter { filterTab.matches(it.verificationStatus) }
}

data class AdminCompanyDetailsUiState(
    val isLoading: Boolean = true,
    val company: CompanyModel? = null,
    val documents: List<CompanyDocumentModel> = emptyList(),
    val isProcessing: Boolean = false,
    val errorMessage: String? = null
)

sealed class AdminCompanyEvent {
    data class ShowMessage(val message: String) : AdminCompanyEvent()
}

class AdminCompanyViewModel(
    private val authRepo: AuthRepo,
    private val adminRepo: AdminRepo,
    private val companyRepo: CompanyRepo
) : ViewModel() {

    private val _companiesUiState = MutableStateFlow(AdminCompaniesUiState())
    val companiesUiState: StateFlow<AdminCompaniesUiState> = _companiesUiState.asStateFlow()

    private val _detailsUiState = MutableStateFlow(AdminCompanyDetailsUiState())
    val detailsUiState: StateFlow<AdminCompanyDetailsUiState> = _detailsUiState.asStateFlow()

    private val _events = Channel<AdminCompanyEvent>(Channel.BUFFERED)
    val events: Flow<AdminCompanyEvent> = _events.receiveAsFlow()

    fun loadCompanies() {
        viewModelScope.launch {
            _companiesUiState.value = _companiesUiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = adminRepo.getAllCompanies()) {
                is ResultState.Success -> _companiesUiState.value =
                    _companiesUiState.value.copy(isLoading = false, companies = result.data)

                is ResultState.Error -> _companiesUiState.value =
                    _companiesUiState.value.copy(isLoading = false, errorMessage = result.message)

                else -> Unit
            }
        }
    }

    fun setFilterTab(tab: AdminCompanyFilterTab) {
        _companiesUiState.value = _companiesUiState.value.copy(filterTab = tab)
    }

    /** Quick approve/reject directly from the companies list, without opening company details first. */
    fun approveCompanyFromList(companyId: String) {
        val adminId = authRepo.currentUserId() ?: return
        if (_companiesUiState.value.processingCompanyId != null) return

        viewModelScope.launch {
            _companiesUiState.value = _companiesUiState.value.copy(processingCompanyId = companyId)
            val result = adminRepo.approveCompany(adminId, companyId)
            _companiesUiState.value = _companiesUiState.value.copy(processingCompanyId = null)
            when (result) {
                is ResultState.Success -> {
                    _events.send(AdminCompanyEvent.ShowMessage("Company approved."))
                    loadCompanies()
                }

                is ResultState.Error -> _events.send(AdminCompanyEvent.ShowMessage(result.message))
                else -> Unit
            }
        }
    }

    fun rejectCompanyFromList(companyId: String, reason: String) {
        val adminId = authRepo.currentUserId() ?: return
        if (_companiesUiState.value.processingCompanyId != null) return

        viewModelScope.launch {
            _companiesUiState.value = _companiesUiState.value.copy(processingCompanyId = companyId)
            val result = adminRepo.rejectCompany(adminId, companyId, reason)
            _companiesUiState.value = _companiesUiState.value.copy(processingCompanyId = null)
            when (result) {
                is ResultState.Success -> {
                    _events.send(AdminCompanyEvent.ShowMessage("Company rejected."))
                    loadCompanies()
                }

                is ResultState.Error -> _events.send(AdminCompanyEvent.ShowMessage(result.message))
                else -> Unit
            }
        }
    }

    fun loadCompanyDetails(companyId: String) {
        viewModelScope.launch {
            _detailsUiState.value = AdminCompanyDetailsUiState(isLoading = true)
            val companyResult = adminRepo.getCompanyById(companyId)
            val documentsResult = companyRepo.getDocuments(companyId)
            if (companyResult is ResultState.Success) {
                _detailsUiState.value = AdminCompanyDetailsUiState(
                    isLoading = false,
                    company = companyResult.data,
                    documents = (documentsResult as? ResultState.Success)?.data ?: emptyList()
                )
            } else {
                _detailsUiState.value = AdminCompanyDetailsUiState(
                    isLoading = false,
                    errorMessage = (companyResult as? ResultState.Error)?.message ?: "Company not found."
                )
            }
        }
    }

    fun approveCompany() {
        val adminId = authRepo.currentUserId() ?: return
        val companyId = _detailsUiState.value.company?.id ?: return
        if (_detailsUiState.value.isProcessing) return

        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isProcessing = true)
            when (val result = adminRepo.approveCompany(adminId, companyId)) {
                is ResultState.Success -> {
                    _events.send(AdminCompanyEvent.ShowMessage("Company approved."))
                    loadCompanyDetails(companyId)
                }

                is ResultState.Error -> {
                    _detailsUiState.value = _detailsUiState.value.copy(isProcessing = false)
                    _events.send(AdminCompanyEvent.ShowMessage(result.message))
                }

                else -> Unit
            }
        }
    }

    fun rejectCompany(reason: String) {
        val adminId = authRepo.currentUserId() ?: return
        val companyId = _detailsUiState.value.company?.id ?: return
        if (_detailsUiState.value.isProcessing) return

        viewModelScope.launch {
            _detailsUiState.value = _detailsUiState.value.copy(isProcessing = true)
            when (val result = adminRepo.rejectCompany(adminId, companyId, reason)) {
                is ResultState.Success -> {
                    _events.send(AdminCompanyEvent.ShowMessage("Company rejected."))
                    loadCompanyDetails(companyId)
                }

                is ResultState.Error -> {
                    _detailsUiState.value = _detailsUiState.value.copy(isProcessing = false)
                    _events.send(AdminCompanyEvent.ShowMessage(result.message))
                }

                else -> Unit
            }
        }
    }
}
