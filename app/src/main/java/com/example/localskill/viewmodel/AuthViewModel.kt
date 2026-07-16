package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.repo.AuthRepo
import com.example.localskill.utils.Constants
import com.example.localskill.utils.ResultState
import com.example.localskill.utils.ValidationUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class JobSeekerRegistrationUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class CompanyRegistrationUiState(
    val companyName: String = "",
    val contactPersonName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val companyNameError: String? = null,
    val contactPersonNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class PasswordResetUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null
)

data class EmailVerificationUiState(
    val email: String = "",
    val isChecking: Boolean = false,
    val isResending: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val isVerified: Boolean = false,
    val errorMessage: String? = null
)

sealed class AuthEvent {
    data object LoginSuccess : AuthEvent()
    data object JobSeekerRegistrationSuccess : AuthEvent()
    data object CompanyRegistrationSuccess : AuthEvent()
    data object EmailVerificationConfirmed : AuthEvent()
    data class ShowMessage(val message: String) : AuthEvent()
}

class AuthViewModel(private val authRepo: AuthRepo) : ViewModel() {

    fun currentEmailOrEmpty(): String = authRepo.currentUserEmail().orEmpty()

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _jobSeekerRegistrationUiState = MutableStateFlow(JobSeekerRegistrationUiState())
    val jobSeekerRegistrationUiState: StateFlow<JobSeekerRegistrationUiState> =
        _jobSeekerRegistrationUiState.asStateFlow()

    private val _companyRegistrationUiState = MutableStateFlow(CompanyRegistrationUiState())
    val companyRegistrationUiState: StateFlow<CompanyRegistrationUiState> =
        _companyRegistrationUiState.asStateFlow()

    private val _passwordResetUiState = MutableStateFlow(PasswordResetUiState())
    val passwordResetUiState: StateFlow<PasswordResetUiState> = _passwordResetUiState.asStateFlow()

    private val _emailVerificationUiState = MutableStateFlow(EmailVerificationUiState())
    val emailVerificationUiState: StateFlow<EmailVerificationUiState> = _emailVerificationUiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    // ---------------------------------------------------------------------
    // Login
    // ---------------------------------------------------------------------

    fun onLoginEmailChanged(value: String) {
        _loginUiState.value = _loginUiState.value.copy(email = value, emailError = null, errorMessage = null)
    }

    fun onLoginPasswordChanged(value: String) {
        _loginUiState.value = _loginUiState.value.copy(password = value, passwordError = null, errorMessage = null)
    }

    fun onLoginPasswordVisibilityToggled() {
        _loginUiState.value = _loginUiState.value.copy(isPasswordVisible = !_loginUiState.value.isPasswordVisible)
    }

    fun submitLogin() {
        val state = _loginUiState.value
        if (state.isLoading) return

        val emailError = ValidationUtils.validateEmail(state.email)
        val passwordError = if (state.password.isEmpty()) "Password is required" else null
        if (emailError != null || passwordError != null) {
            _loginUiState.value = state.copy(emailError = emailError, passwordError = passwordError)
            return
        }

        viewModelScope.launch {
            _loginUiState.value = state.copy(isLoading = true, errorMessage = null)
            when (val result = authRepo.login(state.email.trim(), state.password)) {
                is ResultState.Success -> {
                    _loginUiState.value = LoginUiState()
                    _events.send(AuthEvent.LoginSuccess)
                }

                is ResultState.Error -> _loginUiState.value = _loginUiState.value.copy(
                    isLoading = false,
                    password = "",
                    errorMessage = result.message
                )

                else -> Unit
            }
        }
    }

    // ---------------------------------------------------------------------
    // Job Seeker registration
    // ---------------------------------------------------------------------

    fun onJobSeekerFullNameChanged(value: String) {
        _jobSeekerRegistrationUiState.value =
            _jobSeekerRegistrationUiState.value.copy(fullName = value, fullNameError = null, errorMessage = null)
    }

    fun onJobSeekerEmailChanged(value: String) {
        _jobSeekerRegistrationUiState.value =
            _jobSeekerRegistrationUiState.value.copy(email = value, emailError = null, errorMessage = null)
    }

    fun onJobSeekerPhoneChanged(value: String) {
        _jobSeekerRegistrationUiState.value =
            _jobSeekerRegistrationUiState.value.copy(phone = value, phoneError = null, errorMessage = null)
    }

    fun onJobSeekerAddressChanged(value: String) {
        _jobSeekerRegistrationUiState.value =
            _jobSeekerRegistrationUiState.value.copy(address = value, addressError = null, errorMessage = null)
    }

    fun onJobSeekerPasswordChanged(value: String) {
        _jobSeekerRegistrationUiState.value =
            _jobSeekerRegistrationUiState.value.copy(password = value, passwordError = null, errorMessage = null)
    }

    fun onJobSeekerConfirmPasswordChanged(value: String) {
        _jobSeekerRegistrationUiState.value = _jobSeekerRegistrationUiState.value.copy(
            confirmPassword = value,
            confirmPasswordError = null,
            errorMessage = null
        )
    }

    fun onJobSeekerPasswordVisibilityToggled() {
        _jobSeekerRegistrationUiState.value = _jobSeekerRegistrationUiState.value.copy(
            isPasswordVisible = !_jobSeekerRegistrationUiState.value.isPasswordVisible
        )
    }

    fun onJobSeekerConfirmPasswordVisibilityToggled() {
        _jobSeekerRegistrationUiState.value = _jobSeekerRegistrationUiState.value.copy(
            isConfirmPasswordVisible = !_jobSeekerRegistrationUiState.value.isConfirmPasswordVisible
        )
    }

    fun onJobSeekerTermsAcceptedChanged(accepted: Boolean) {
        _jobSeekerRegistrationUiState.value =
            _jobSeekerRegistrationUiState.value.copy(termsAccepted = accepted, termsError = null)
    }

    fun submitJobSeekerRegistration() {
        val state = _jobSeekerRegistrationUiState.value
        if (state.isLoading) return

        val fullNameError = ValidationUtils.validateFullName(state.fullName)
        val emailError = ValidationUtils.validateEmail(state.email)
        val phoneError = ValidationUtils.validatePhone(state.phone)
        val addressError = ValidationUtils.validateAddress(state.address)
        val passwordError = ValidationUtils.validatePassword(state.password)
        val confirmPasswordError = ValidationUtils.validateConfirmPassword(state.password, state.confirmPassword)
        val termsError = ValidationUtils.validateTermsAccepted(state.termsAccepted)

        if (listOf(
                fullNameError, emailError, phoneError, addressError,
                passwordError, confirmPasswordError, termsError
            ).any { it != null }
        ) {
            _jobSeekerRegistrationUiState.value = state.copy(
                fullNameError = fullNameError,
                emailError = emailError,
                phoneError = phoneError,
                addressError = addressError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                termsError = termsError
            )
            return
        }

        viewModelScope.launch {
            _jobSeekerRegistrationUiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = authRepo.registerJobSeeker(
                fullName = state.fullName.trim(),
                email = state.email.trim(),
                phone = state.phone.trim(),
                address = state.address.trim(),
                password = state.password
            )
            when (result) {
                is ResultState.Success -> {
                    _jobSeekerRegistrationUiState.value = JobSeekerRegistrationUiState()
                    _events.send(AuthEvent.JobSeekerRegistrationSuccess)
                }

                is ResultState.Error -> _jobSeekerRegistrationUiState.value =
                    _jobSeekerRegistrationUiState.value.copy(isLoading = false, errorMessage = result.message)

                else -> Unit
            }
        }
    }

    // ---------------------------------------------------------------------
    // Company registration
    // ---------------------------------------------------------------------

    fun onCompanyNameChanged(value: String) {
        _companyRegistrationUiState.value =
            _companyRegistrationUiState.value.copy(companyName = value, companyNameError = null, errorMessage = null)
    }

    fun onCompanyContactPersonNameChanged(value: String) {
        _companyRegistrationUiState.value = _companyRegistrationUiState.value.copy(
            contactPersonName = value,
            contactPersonNameError = null,
            errorMessage = null
        )
    }

    fun onCompanyEmailChanged(value: String) {
        _companyRegistrationUiState.value =
            _companyRegistrationUiState.value.copy(email = value, emailError = null, errorMessage = null)
    }

    fun onCompanyPhoneChanged(value: String) {
        _companyRegistrationUiState.value =
            _companyRegistrationUiState.value.copy(phone = value, phoneError = null, errorMessage = null)
    }

    fun onCompanyAddressChanged(value: String) {
        _companyRegistrationUiState.value =
            _companyRegistrationUiState.value.copy(address = value, addressError = null, errorMessage = null)
    }

    fun onCompanyPasswordChanged(value: String) {
        _companyRegistrationUiState.value =
            _companyRegistrationUiState.value.copy(password = value, passwordError = null, errorMessage = null)
    }

    fun onCompanyConfirmPasswordChanged(value: String) {
        _companyRegistrationUiState.value = _companyRegistrationUiState.value.copy(
            confirmPassword = value,
            confirmPasswordError = null,
            errorMessage = null
        )
    }

    fun onCompanyPasswordVisibilityToggled() {
        _companyRegistrationUiState.value = _companyRegistrationUiState.value.copy(
            isPasswordVisible = !_companyRegistrationUiState.value.isPasswordVisible
        )
    }

    fun onCompanyConfirmPasswordVisibilityToggled() {
        _companyRegistrationUiState.value = _companyRegistrationUiState.value.copy(
            isConfirmPasswordVisible = !_companyRegistrationUiState.value.isConfirmPasswordVisible
        )
    }

    fun onCompanyTermsAcceptedChanged(accepted: Boolean) {
        _companyRegistrationUiState.value =
            _companyRegistrationUiState.value.copy(termsAccepted = accepted, termsError = null)
    }

    fun submitCompanyRegistration() {
        val state = _companyRegistrationUiState.value
        if (state.isLoading) return

        val companyNameError = ValidationUtils.validateCompanyName(state.companyName)
        val contactPersonNameError = ValidationUtils.validateFullName(state.contactPersonName)
        val emailError = ValidationUtils.validateEmail(state.email)
        val phoneError = ValidationUtils.validatePhone(state.phone)
        val addressError = ValidationUtils.validateAddress(state.address)
        val passwordError = ValidationUtils.validatePassword(state.password)
        val confirmPasswordError = ValidationUtils.validateConfirmPassword(state.password, state.confirmPassword)
        val termsError = ValidationUtils.validateTermsAccepted(state.termsAccepted)

        if (listOf(
                companyNameError, contactPersonNameError, emailError, phoneError,
                addressError, passwordError, confirmPasswordError, termsError
            ).any { it != null }
        ) {
            _companyRegistrationUiState.value = state.copy(
                companyNameError = companyNameError,
                contactPersonNameError = contactPersonNameError,
                emailError = emailError,
                phoneError = phoneError,
                addressError = addressError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                termsError = termsError
            )
            return
        }

        viewModelScope.launch {
            _companyRegistrationUiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = authRepo.registerCompany(
                companyName = state.companyName.trim(),
                contactPersonName = state.contactPersonName.trim(),
                email = state.email.trim(),
                phone = state.phone.trim(),
                address = state.address.trim(),
                password = state.password
            )
            when (result) {
                is ResultState.Success -> {
                    _companyRegistrationUiState.value = CompanyRegistrationUiState()
                    _events.send(AuthEvent.CompanyRegistrationSuccess)
                }

                is ResultState.Error -> _companyRegistrationUiState.value =
                    _companyRegistrationUiState.value.copy(isLoading = false, errorMessage = result.message)

                else -> Unit
            }
        }
    }

    // ---------------------------------------------------------------------
    // Password reset
    // ---------------------------------------------------------------------

    fun onPasswordResetEmailChanged(value: String) {
        _passwordResetUiState.value = _passwordResetUiState.value.copy(email = value, emailError = null)
    }

    fun submitPasswordReset() {
        val state = _passwordResetUiState.value
        if (state.isLoading) return

        val emailError = ValidationUtils.validateEmail(state.email)
        if (emailError != null) {
            _passwordResetUiState.value = state.copy(emailError = emailError)
            return
        }

        viewModelScope.launch {
            _passwordResetUiState.value = state.copy(isLoading = true, errorMessage = null)
            // A neutral outcome is shown regardless of whether the account exists,
            // to avoid leaking account-enumeration information.
            authRepo.sendPasswordResetEmail(state.email.trim())
            _passwordResetUiState.value = _passwordResetUiState.value.copy(isLoading = false, isSubmitted = true)
            _events.send(AuthEvent.ShowMessage("If an account exists for this email, a reset link has been sent."))
        }
    }

    fun resetPasswordResetState() {
        _passwordResetUiState.value = PasswordResetUiState()
    }

    // ---------------------------------------------------------------------
    // Email verification
    // ---------------------------------------------------------------------

    fun prepareEmailVerification(email: String) {
        _emailVerificationUiState.value = EmailVerificationUiState(email = email)
    }

    fun resendVerificationEmail() {
        val state = _emailVerificationUiState.value
        if (state.isResending || state.resendCooldownSeconds > 0) return

        viewModelScope.launch {
            _emailVerificationUiState.value = state.copy(isResending = true, errorMessage = null)
            when (val result = authRepo.sendEmailVerification()) {
                is ResultState.Success -> startResendCooldown()
                is ResultState.Error -> _emailVerificationUiState.value =
                    _emailVerificationUiState.value.copy(isResending = false, errorMessage = result.message)

                else -> Unit
            }
        }
    }

    private fun startResendCooldown() {
        viewModelScope.launch {
            for (remaining in Constants.EMAIL_VERIFICATION_RESEND_COOLDOWN_SECONDS downTo 0) {
                _emailVerificationUiState.value = _emailVerificationUiState.value.copy(
                    isResending = false,
                    resendCooldownSeconds = remaining
                )
                if (remaining > 0) delay(1000)
            }
        }
    }

    fun checkEmailVerified() {
        val state = _emailVerificationUiState.value
        if (state.isChecking) return

        viewModelScope.launch {
            _emailVerificationUiState.value = state.copy(isChecking = true, errorMessage = null)
            when (val result = authRepo.reloadAndCheckEmailVerified()) {
                is ResultState.Success -> {
                    _emailVerificationUiState.value =
                        _emailVerificationUiState.value.copy(isChecking = false, isVerified = result.data)
                    if (result.data) {
                        _events.send(AuthEvent.EmailVerificationConfirmed)
                    } else {
                        _events.send(AuthEvent.ShowMessage("Email not verified yet. Please check your inbox."))
                    }
                }

                is ResultState.Error -> _emailVerificationUiState.value =
                    _emailVerificationUiState.value.copy(isChecking = false, errorMessage = result.message)

                else -> Unit
            }
        }
    }

    fun signOutFromVerification() {
        authRepo.logout()
        _emailVerificationUiState.value = EmailVerificationUiState()
    }
}
