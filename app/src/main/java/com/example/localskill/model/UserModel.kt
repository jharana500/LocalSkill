package com.example.localskill.model

data class UserModel(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val profileImageUrl: String = "",
    val role: String = UserRole.JOB_SEEKER.name,
    val accountStatus: String = AccountStatus.ACTIVE.name,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "fullName" to fullName,
        "email" to email,
        "phone" to phone,
        "address" to address,
        "profileImageUrl" to profileImageUrl,
        "role" to role,
        "accountStatus" to accountStatus,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}
