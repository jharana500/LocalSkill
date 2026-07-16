package com.example.localskill.utils

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseException
import com.google.firebase.storage.StorageException
import java.io.IOException

/**
 * Translates Firebase exceptions into user-facing messages. Never surfaces
 * raw exception class names, error codes, or stack traces to the UI.
 */
object FirebaseErrorMapper {

    fun map(throwable: Throwable?): String = when (throwable) {
        is FirebaseAuthWeakPasswordException -> "Choose a stronger password."
        is FirebaseAuthUserCollisionException -> "An account with this email already exists."
        is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
        is FirebaseAuthInvalidUserException -> "No account found for this email."
        is FirebaseAuthException -> messageForAuthCode(throwable.errorCode)
        is FirebaseNetworkException -> "Check your internet connection and try again."
        is DatabaseException -> messageForDatabase(throwable.message.orEmpty())
        is StorageException -> messageForStorage(throwable.errorCode)
        is IOException -> "We could not reach the service. Check your connection and try again."
        is FirebaseException -> messageForFirebase(throwable.message.orEmpty())
        else -> messageForKnownBusinessError(throwable?.message.orEmpty())
    }

    private fun messageForAuthCode(errorCode: String): String = when (errorCode) {
        "ERROR_NETWORK_REQUEST_FAILED" -> "Check your internet connection and try again."
        "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please wait and try again."
        "ERROR_USER_DISABLED" -> "This account has been disabled."
        else -> "Something went wrong. Please try again."
    }

    private fun messageForDatabase(message: String): String {
        val normalized = message.lowercase()
        return when {
            "permission" in normalized -> "You do not have permission to perform this action."
            "network" in normalized || "disconnected" in normalized -> "Check your internet connection and try again."
            "not found" in normalized || "missing" in normalized -> "This record is no longer available."
            else -> "We could not update the database. Please try again."
        }
    }

    private fun messageForStorage(errorCode: Int): String = when (errorCode) {
        StorageException.ERROR_OBJECT_NOT_FOUND -> "This file is no longer available."
        StorageException.ERROR_NOT_AUTHENTICATED -> "Sign in again to access this file."
        StorageException.ERROR_NOT_AUTHORIZED -> "You do not have permission to access this file."
        StorageException.ERROR_QUOTA_EXCEEDED -> "Storage quota has been exceeded. Please try again later."
        StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> "The upload took too long. Check your connection and try again."
        StorageException.ERROR_CANCELED -> "The upload was cancelled."
        else -> "We could not complete the file operation. Please try again."
    }

    private fun messageForFirebase(message: String): String {
        val normalized = message.lowercase()
        return when {
            "permission" in normalized -> "You do not have permission to perform this action."
            "network" in normalized -> "Check your internet connection and try again."
            "quota" in normalized -> "The service is temporarily unavailable. Please try again later."
            else -> "Something went wrong. Please try again."
        }
    }

    private fun messageForKnownBusinessError(message: String): String {
        val normalized = message.lowercase()
        return when {
            "already applied" in normalized -> "You have already applied to this job."
            "already saved" in normalized -> "This job is already saved."
            "status change is not allowed" in normalized || "cannot be moved" in normalized -> "This status change is not allowed."
            "not verified" in normalized -> "Your company must be verified before doing this."
            "suspended" in normalized -> "This account is suspended. Contact support if this seems wrong."
            "expired" in normalized || "removed job" in normalized -> "This job is no longer accepting applications."
            "already have an open report" in normalized || "already resolved" in normalized -> "This report has already been handled."
            "not found" in normalized || "no longer available" in normalized -> "This record is no longer available."
            else -> "Something went wrong. Please try again."
        }
    }
}
