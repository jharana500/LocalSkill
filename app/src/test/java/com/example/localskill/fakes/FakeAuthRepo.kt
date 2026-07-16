package com.example.localskill.fakes

import com.example.localskill.model.AuthSessionModel
import com.example.localskill.repo.AuthRepo
import com.example.localskill.utils.ResultState

class FakeAuthRepo : AuthRepo {

    var loggedIn: Boolean = false
    var currentEmail: String? = "user@example.com"

    var loginResult: ResultState<String> = ResultState.Success("uid-123")
    var registerJobSeekerResult: ResultState<String> = ResultState.Success("uid-123")
    var registerCompanyResult: ResultState<String> = ResultState.Success("uid-123")
    var sendPasswordResetResult: ResultState<Unit> = ResultState.Success(Unit)
    var sendEmailVerificationResult: ResultState<Unit> = ResultState.Success(Unit)
    var reloadAndCheckEmailVerifiedResult: ResultState<Boolean> = ResultState.Success(true)
    var restoreSessionResult: ResultState<AuthSessionModel?> = ResultState.Success(null)

    var lastLoginEmail: String? = null
    var lastLoginPassword: String? = null
    var lastJobSeekerRegistration: JobSeekerRegistrationArgs? = null
    var lastCompanyRegistration: CompanyRegistrationArgs? = null
    var logoutCalled: Boolean = false

    data class JobSeekerRegistrationArgs(
        val fullName: String,
        val email: String,
        val phone: String,
        val address: String,
        val password: String
    )

    data class CompanyRegistrationArgs(
        val companyName: String,
        val contactPersonName: String,
        val email: String,
        val phone: String,
        val address: String,
        val password: String
    )

    override fun currentUserId(): String? = if (loggedIn) "uid-123" else null

    override fun currentUserEmail(): String? = currentEmail

    override fun isUserLoggedIn(): Boolean = loggedIn

    override suspend fun login(email: String, password: String): ResultState<String> {
        lastLoginEmail = email
        lastLoginPassword = password
        return loginResult
    }

    override suspend fun registerJobSeeker(
        fullName: String,
        email: String,
        phone: String,
        address: String,
        password: String
    ): ResultState<String> {
        lastJobSeekerRegistration = JobSeekerRegistrationArgs(fullName, email, phone, address, password)
        return registerJobSeekerResult
    }

    override suspend fun registerCompany(
        companyName: String,
        contactPersonName: String,
        email: String,
        phone: String,
        address: String,
        password: String
    ): ResultState<String> {
        lastCompanyRegistration =
            CompanyRegistrationArgs(companyName, contactPersonName, email, phone, address, password)
        return registerCompanyResult
    }

    override suspend fun sendPasswordResetEmail(email: String): ResultState<Unit> = sendPasswordResetResult

    override suspend fun sendEmailVerification(): ResultState<Unit> = sendEmailVerificationResult

    override suspend fun reloadAndCheckEmailVerified(): ResultState<Boolean> = reloadAndCheckEmailVerifiedResult

    override suspend fun restoreSession(): ResultState<AuthSessionModel?> = restoreSessionResult

    override fun logout() {
        logoutCalled = true
        loggedIn = false
    }
}
