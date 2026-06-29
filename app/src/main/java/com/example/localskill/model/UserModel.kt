package com.example.localskill.model

data class UserModel(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole? = null,
    val location: String = "",
    val area: String = "",
    val city: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationUpdatedAt: Long = 0L,
    val bio: String = "",
    val experience: String = "",
    val availability: String = "",
    val profileImageUrl: String = "",
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

enum class UserRole {
    WORKER,
    EMPLOYER
}
