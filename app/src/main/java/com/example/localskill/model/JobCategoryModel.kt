package com.example.localskill.model

data class JobCategoryModel(
    val id: String = "",
    val name: String = "",
    val jobCount: Int = 0,
    val isActive: Boolean = true
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "jobCount" to jobCount,
        "isActive" to isActive
    )
}
