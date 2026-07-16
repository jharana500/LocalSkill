package com.example.localskill.model

data class AuthSessionModel(
    val userId: String,
    val email: String,
    val isEmailVerified: Boolean,
    val role: String,
    val accountStatus: String
)
