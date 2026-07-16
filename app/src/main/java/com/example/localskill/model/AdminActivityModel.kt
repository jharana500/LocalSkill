package com.example.localskill.model

/** Append-only from normal client workflows — created alongside the action it records. */
data class AdminActivityModel(
    val id: String = "",
    val adminId: String = "",
    val actionType: String = AdminActivityType.COMPANY_APPROVED.name,
    val targetType: String = "",
    val targetId: String = "",
    val summary: String = "",
    val createdAt: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "adminId" to adminId,
        "actionType" to actionType,
        "targetType" to targetType,
        "targetId" to targetId,
        "summary" to summary,
        "createdAt" to createdAt
    )
}
