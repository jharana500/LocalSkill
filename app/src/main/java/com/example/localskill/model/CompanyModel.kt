package com.example.localskill.model

data class CompanyModel(
    val id: String = "",
    val ownerUserId: String = "",
    val companyName: String = "",
    val contactPersonName: String = "",
    val email: String = "",
    val phone: String = "",
    val website: String = "",
    val description: String = "",
    val industry: String = "",
    val employeeCountRange: String = "",
    val registrationNumber: String = "",
    val panNumber: String = "",
    val address: String = "",
    val city: String = "",
    val district: String = "",
    val logoUrl: String = "",
    val verificationStatus: String = CompanyVerificationStatus.DRAFT.name,
    val verificationSubmittedAt: Long = 0L,
    val verifiedAt: Long = 0L,
    val verifiedBy: String = "",
    val rejectionReason: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    /** Editable business fields only — never the admin-controlled verification fields. */
    fun toEditableMap(): Map<String, Any> = mapOf(
        "companyName" to companyName,
        "contactPersonName" to contactPersonName,
        "phone" to phone,
        "website" to website,
        "description" to description,
        "industry" to industry,
        "employeeCountRange" to employeeCountRange,
        "registrationNumber" to registrationNumber,
        "panNumber" to panNumber,
        "address" to address,
        "city" to city,
        "district" to district,
        "updatedAt" to System.currentTimeMillis()
    )

    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "ownerUserId" to ownerUserId,
        "companyName" to companyName,
        "contactPersonName" to contactPersonName,
        "email" to email,
        "phone" to phone,
        "website" to website,
        "description" to description,
        "industry" to industry,
        "employeeCountRange" to employeeCountRange,
        "registrationNumber" to registrationNumber,
        "panNumber" to panNumber,
        "address" to address,
        "city" to city,
        "district" to district,
        "logoUrl" to logoUrl,
        "verificationStatus" to verificationStatus,
        "verificationSubmittedAt" to verificationSubmittedAt,
        "verifiedAt" to verifiedAt,
        "verifiedBy" to verifiedBy,
        "rejectionReason" to rejectionReason,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    val isVerified: Boolean
        get() = verificationStatus == CompanyVerificationStatus.VERIFIED.name

    val isRejected: Boolean
        get() = verificationStatus == CompanyVerificationStatus.REJECTED.name

    val isPending: Boolean
        get() = verificationStatus == CompanyVerificationStatus.PENDING.name

    val isDraft: Boolean
        get() = verificationStatus == CompanyVerificationStatus.DRAFT.name

    /** Sections required before verification can be submitted. */
    val missingProfileSections: List<String>
        get() = buildList {
            if (companyName.isBlank()) add("Company name")
            if (description.isBlank()) add("Company description")
            if (industry.isBlank()) add("Industry")
            if (address.isBlank()) add("Address")
            if (city.isBlank()) add("City")
            if (registrationNumber.isBlank()) add("Registration number")
            if (logoUrl.isBlank()) add("Company logo")
        }

    val profileCompletionPercentage: Int
        get() {
            val totalSections = 7
            val completed = totalSections - missingProfileSections.size
            return ((completed.toFloat() / totalSections) * 100).toInt().coerceIn(0, 100)
        }
}
