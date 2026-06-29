package com.example.localskill.utils

import android.util.Patterns

object Validators {
    fun email(value: String): String? = when {
        value.isBlank() -> "Email is required"
        !Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches() -> "Enter a valid email"
        else -> null
    }

    fun password(value: String): String? = when {
        value.isBlank() -> "Password is required"
        value.length < 6 -> "Password must be at least 6 characters"
        else -> null
    }

    fun required(value: String, label: String): String? =
        if (value.isBlank()) "$label is required" else null

    fun confirmPassword(password: String, confirmPassword: String): String? =
        if (password != confirmPassword) "Passwords do not match" else null

    fun optionalUrl(value: String, label: String): String? = when {
        value.isBlank() -> null
        !Patterns.WEB_URL.matcher(value.trim()).matches() -> "Enter a valid $label"
        else -> null
    }
}
