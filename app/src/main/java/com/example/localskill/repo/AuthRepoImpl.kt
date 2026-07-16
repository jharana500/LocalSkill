package com.example.localskill.repo

import com.example.localskill.model.AccountStatus
import com.example.localskill.model.AuthSessionModel
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.model.UserModel
import com.example.localskill.model.UserRole
import com.example.localskill.utils.Constants
import com.example.localskill.utils.FirebaseErrorMapper
import com.example.localskill.utils.ResultState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AuthRepoImpl(
    private val auth: FirebaseAuth,
    database: FirebaseDatabase,
    private val userRepo: UserRepo
) : AuthRepo {

    private val companiesRef: DatabaseReference = database.getReference(Constants.COMPANIES_NODE)

    override fun currentUserId(): String? = auth.currentUser?.uid

    override fun currentUserEmail(): String? = auth.currentUser?.email

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null

    override suspend fun login(email: String, password: String): ResultState<String> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        ResultState.Success(result.user?.uid.orEmpty())
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun registerJobSeeker(
        fullName: String,
        email: String,
        phone: String,
        address: String,
        password: String
    ): ResultState<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid.orEmpty()
            val now = System.currentTimeMillis()

            val profile = UserModel(
                id = uid,
                fullName = fullName,
                email = email,
                phone = phone,
                address = address,
                role = UserRole.JOB_SEEKER.name,
                accountStatus = AccountStatus.ACTIVE.name,
                createdAt = now,
                updatedAt = now
            )

            val profileResult = userRepo.addUser(profile)
            if (profileResult is ResultState.Error) {
                authResult.user?.delete()?.await()
                return ResultState.Error(profileResult.message, profileResult.throwable)
            }

            authResult.user?.sendEmailVerification()?.await()
            ResultState.Success(uid)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun registerCompany(
        companyName: String,
        contactPersonName: String,
        email: String,
        phone: String,
        address: String,
        password: String
    ): ResultState<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid.orEmpty()
            val now = System.currentTimeMillis()

            val profile = UserModel(
                id = uid,
                fullName = contactPersonName,
                email = email,
                phone = phone,
                address = address,
                role = UserRole.COMPANY.name,
                accountStatus = AccountStatus.PENDING.name,
                createdAt = now,
                updatedAt = now
            )

            val profileResult = userRepo.addUser(profile)
            if (profileResult is ResultState.Error) {
                authResult.user?.delete()?.await()
                return ResultState.Error(profileResult.message, profileResult.throwable)
            }

            val company = CompanyModel(
                id = uid,
                ownerUserId = uid,
                companyName = companyName,
                contactPersonName = contactPersonName,
                email = email,
                phone = phone,
                address = address,
                verificationStatus = CompanyVerificationStatus.DRAFT.name,
                createdAt = now,
                updatedAt = now
            )
            companiesRef.child(uid).setValue(company.toMap()).await()

            authResult.user?.sendEmailVerification()?.await()
            ResultState.Success(uid)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): ResultState<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun sendEmailVerification(): ResultState<Unit> = try {
        auth.currentUser?.sendEmailVerification()?.await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun reloadAndCheckEmailVerified(): ResultState<Boolean> = try {
        auth.currentUser?.reload()?.await()
        ResultState.Success(auth.currentUser?.isEmailVerified == true)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun restoreSession(): ResultState<AuthSessionModel?> {
        val firebaseUser = auth.currentUser ?: return ResultState.Success(null)
        return try {
            firebaseUser.reload().await()
            val refreshedUser = auth.currentUser ?: return ResultState.Success(null)
            when (val profileResult = userRepo.getUserById(refreshedUser.uid)) {
                is ResultState.Success -> ResultState.Success(
                    AuthSessionModel(
                        userId = refreshedUser.uid,
                        email = refreshedUser.email.orEmpty(),
                        isEmailVerified = refreshedUser.isEmailVerified,
                        role = profileResult.data.role,
                        accountStatus = profileResult.data.accountStatus
                    )
                )
                is ResultState.Error -> ResultState.Error(profileResult.message, profileResult.throwable)
                else -> ResultState.Success(null)
            }
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override fun logout() {
        auth.signOut()
    }
}
