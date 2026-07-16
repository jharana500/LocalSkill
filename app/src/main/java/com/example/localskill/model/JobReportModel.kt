package com.example.localskill.model

data class JobReportModel(
    val id: String = "",
    val reporterId: String = "",
    val targetType: String = ReportType.JOB.name,
    val targetId: String = "",
    val relatedJobId: String = "",
    val relatedCompanyId: String = "",
    val reason: String = "",
    val description: String = "",
    val evidenceUrl: String = "",
    val status: String = ReportStatus.PENDING.name,
    val resolutionNote: String = "",
    val resolvedBy: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val resolvedAt: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "reporterId" to reporterId,
        "targetType" to targetType,
        "targetId" to targetId,
        "relatedJobId" to relatedJobId,
        "relatedCompanyId" to relatedCompanyId,
        "reason" to reason,
        "description" to description,
        "evidenceUrl" to evidenceUrl,
        "status" to status,
        "resolutionNote" to resolutionNote,
        "resolvedBy" to resolvedBy,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "resolvedAt" to resolvedAt
    )
}
