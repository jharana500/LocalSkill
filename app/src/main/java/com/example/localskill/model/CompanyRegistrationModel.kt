package com.example.localskill.model

enum class VerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED
}

/**
 * Minimal companies/{uid} bootstrap record created at registration time.
 * The full company profile, document upload, and verification workflow
 * belong to a later phase.
 */
data class CompanyRegistrationModel(
    val id: String = "",
    val ownerUserId: String = "",
    val companyName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val verificationStatus: String = VerificationStatus.PENDING.name,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "ownerUserId" to ownerUserId,
        "companyName" to companyName,
        "email" to email,
        "phone" to phone,
        "address" to address,
        "verificationStatus" to verificationStatus,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}
