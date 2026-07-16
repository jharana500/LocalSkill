package com.example.localskill.utils

import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

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
        is FirebaseAuthException -> messageForCode(throwable.errorCode)
        else -> "Something went wrong. Please try again."
    }

    private fun messageForCode(errorCode: String): String = when (errorCode) {
        "ERROR_NETWORK_REQUEST_FAILED" -> "Check your internet connection and try again."
        "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please wait and try again."
        "ERROR_USER_DISABLED" -> "This account has been disabled."
        else -> "Something went wrong. Please try again."
    }
}
