package com.example.localskill.utils

enum class PasswordStrength {
    NONE,
    WEAK,
    MEDIUM,
    STRONG
}

object ValidationUtils {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val NEPAL_PHONE_REGEX = Regex("^(\\+977[- ]?)?9[6-9]\\d{8}$")

    fun validateFullName(value: String): String? = when {
        value.isEmpty() -> "Full name is required"
        value.trim() != value -> "Remove leading or trailing spaces"
        value.trim().length < 3 -> "Enter your full name"
        else -> null
    }

    fun validateCompanyName(value: String): String? = when {
        value.isEmpty() -> "Company name is required"
        value.trim() != value -> "Remove leading or trailing spaces"
        value.trim().length < 2 -> "Enter a valid company name"
        else -> null
    }

    fun validateEmail(value: String): String? = when {
        value.isEmpty() -> "Email is required"
        value.trim() != value -> "Remove leading or trailing spaces"
        !EMAIL_REGEX.matches(value) -> "Enter a valid email address"
        else -> null
    }

    fun validatePhone(value: String): String? = when {
        value.isEmpty() -> "Phone number is required"
        value.trim() != value -> "Remove leading or trailing spaces"
        !NEPAL_PHONE_REGEX.matches(value.replace(" ", "")) -> "Enter a valid Nepal phone number"
        else -> null
    }

    fun validateAddress(value: String): String? = when {
        value.isEmpty() -> "City or address is required"
        value.trim() != value -> "Remove leading or trailing spaces"
        else -> null
    }

    fun validatePassword(value: String): String? = when {
        value.isEmpty() -> "Password is required"
        value.length < Constants.MIN_PASSWORD_LENGTH -> "Use at least ${Constants.MIN_PASSWORD_LENGTH} characters"
        value.none { it.isDigit() } -> "Include at least one number"
        value.none { it.isUpperCase() } -> "Include at least one uppercase letter"
        value.none { it.isLowerCase() } -> "Include at least one lowercase letter"
        else -> null
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? = when {
        confirmPassword.isEmpty() -> "Confirm your password"
        confirmPassword != password -> "Passwords do not match"
        else -> null
    }

    fun validateTermsAccepted(accepted: Boolean): String? =
        if (!accepted) "You must accept the Terms and Privacy Policy" else null

    fun passwordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.NONE
        var score = 0
        if (password.length >= Constants.MIN_PASSWORD_LENGTH) score++
        if (password.length >= 12) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return when {
            score <= 1 -> PasswordStrength.WEAK
            score <= 3 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
}
