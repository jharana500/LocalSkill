package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.AccountStatus
import com.example.localskill.model.UserRole
import com.example.localskill.repo.AppPreferencesRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.utils.ResultState
import com.example.localskill.view.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SessionDestination {
    UNKNOWN,
    ONBOARDING,
    ROLE_SELECTION,
    EMAIL_VERIFICATION,
    ACCOUNT_STATUS,
    JOB_SEEKER_ENTRY,
    COMPANY_ENTRY,
    ADMIN_ENTRY
}

data class AppSessionUiState(
    val isLoading: Boolean = true,
    val destination: SessionDestination = SessionDestination.UNKNOWN,
    val activeRole: UserRole = UserRole.JOB_SEEKER,
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,
    /**
     * True for a COMPANY whose accountStatus isn't ACTIVE yet (still DRAFT,
     * submitted PENDING, or REJECTED). The Company graph reads this to
     * gate publishing/applicant-management routes while still allowing
     * profile completion, document upload, and verification status review.
     * The exact verification detail (which of those three states, and any
     * rejection reason) is loaded live from CompanyRepo inside the graph —
     * this flag only decides which mode the graph opens in.
     */
    val companyRestrictedMode: Boolean = false
)

/**
 * Decides where the app should land the user on launch (and after
 * login/logout), based only on trusted state: local onboarding
 * completion, the live Firebase session, and the role/status stored in
 * the user's database profile. Never trusts a role passed through
 * navigation arguments.
 */
class AppSessionViewModel(
    private val authRepo: AuthRepo,
    private val appPreferencesRepo: AppPreferencesRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppSessionUiState())
    val uiState: StateFlow<AppSessionUiState> = _uiState.asStateFlow()

    val appTheme: StateFlow<AppTheme> = appPreferencesRepo.appTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppTheme.SYSTEM
    )

    init {
        evaluateSession()
    }

    fun evaluateSession() {
        viewModelScope.launch {
            _uiState.value = AppSessionUiState(isLoading = true)

            val onboardingCompleted = appPreferencesRepo.isOnboardingCompleted.first()
            if (!onboardingCompleted) {
                _uiState.value = AppSessionUiState(isLoading = false, destination = SessionDestination.ONBOARDING)
                return@launch
            }

            if (!authRepo.isUserLoggedIn()) {
                _uiState.value = AppSessionUiState(isLoading = false, destination = SessionDestination.ROLE_SELECTION)
                return@launch
            }

            when (val result = authRepo.restoreSession()) {
                is ResultState.Success -> {
                    val session = result.data
                    _uiState.value = when {
                        session == null -> AppSessionUiState(
                            isLoading = false,
                            destination = SessionDestination.ROLE_SELECTION
                        )

                        !session.isEmailVerified -> AppSessionUiState(
                            isLoading = false,
                            destination = SessionDestination.EMAIL_VERIFICATION
                        )

                        else -> {
                            val role = runCatching { UserRole.valueOf(session.role) }
                                .getOrDefault(UserRole.JOB_SEEKER)
                            val accountStatus = runCatching { AccountStatus.valueOf(session.accountStatus) }
                                .getOrDefault(AccountStatus.ACTIVE)

                            // A suspended account is always blocked, for every role. A pending
                            // Company is not suspended — it still needs to reach its own graph
                            // to complete verification, so only Job Seekers (who have no
                            // restricted-mode concept) fall back to AccountStatus when merely
                            // non-active. Admin accounts are provisioned externally and are
                            // never expected to be PENDING.
                            val destination = when {
                                accountStatus == AccountStatus.SUSPENDED -> SessionDestination.ACCOUNT_STATUS
                                role == UserRole.JOB_SEEKER && accountStatus != AccountStatus.ACTIVE ->
                                    SessionDestination.ACCOUNT_STATUS
                                role == UserRole.COMPANY -> SessionDestination.COMPANY_ENTRY
                                role == UserRole.ADMIN -> SessionDestination.ADMIN_ENTRY
                                else -> SessionDestination.JOB_SEEKER_ENTRY
                            }

                            AppSessionUiState(
                                isLoading = false,
                                destination = destination,
                                activeRole = role,
                                accountStatus = accountStatus,
                                companyRestrictedMode = role == UserRole.COMPANY && accountStatus != AccountStatus.ACTIVE
                            )
                        }
                    }
                }

                is ResultState.Error -> _uiState.value = AppSessionUiState(
                    isLoading = false,
                    destination = SessionDestination.ROLE_SELECTION
                )

                else -> Unit
            }
        }
    }

    fun logout() {
        authRepo.logout()
        _uiState.value = AppSessionUiState(isLoading = false, destination = SessionDestination.ROLE_SELECTION)
    }
}
