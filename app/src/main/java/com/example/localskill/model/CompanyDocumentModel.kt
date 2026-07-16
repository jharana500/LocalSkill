package com.example.localskill.model

data class CompanyDocumentModel(
    val id: String = "",
    val companyId: String = "",
    val documentType: String = CompanyDocumentType.REGISTRATION_CERTIFICATE.name,
    val fileName: String = "",
    val downloadUrl: String = "",
    val fileSizeBytes: Long = 0L,
    val uploadedAt: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "companyId" to companyId,
        "documentType" to documentType,
        "fileName" to fileName,
        "downloadUrl" to downloadUrl,
        "fileSizeBytes" to fileSizeBytes,
        "uploadedAt" to uploadedAt
    )
}
