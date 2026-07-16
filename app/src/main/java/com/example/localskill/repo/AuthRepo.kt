package com.example.localskill.repo

import com.example.localskill.model.AuthSessionModel
import com.example.localskill.utils.ResultState

/**
 * Owns everything about the authenticated identity: login, registration,
 * logout, password reset, email verification, and session restoration.
 * User profile records themselves live in [UserRepo].
 */
interface AuthRepo {

    fun currentUserId(): String?

    fun currentUserEmail(): String?

    fun isUserLoggedIn(): Boolean

    suspend fun login(email: String, password: String): ResultState<String>

    suspend fun registerJobSeeker(
        fullName: String,
        email: String,
        phone: String,
        address: String,
        password: String
    ): ResultState<String>

    suspend fun registerCompany(
        companyName: String,
        contactPersonName: String,
        email: String,
        phone: String,
        address: String,
        password: String
    ): ResultState<String>

    suspend fun sendPasswordResetEmail(email: String): ResultState<Unit>

    suspend fun sendEmailVerification(): ResultState<Unit>

    suspend fun reloadAndCheckEmailVerified(): ResultState<Boolean>

    suspend fun restoreSession(): ResultState<AuthSessionModel?>

    fun logout()
}
