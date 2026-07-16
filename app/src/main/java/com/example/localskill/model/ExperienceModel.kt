package com.example.localskill.model

data class ExperienceModel(
    val id: String = "",
    val jobTitle: String = "",
    val company: String = "",
    val location: String = "",
    val employmentType: String = "",
    val startDate: Long = 0L,
    val endDate: Long? = null,
    val currentlyWorking: Boolean = false,
    val description: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "jobTitle" to jobTitle,
        "company" to company,
        "location" to location,
        "employmentType" to employmentType,
        "startDate" to startDate,
        "endDate" to endDate,
        "currentlyWorking" to currentlyWorking,
        "description" to description
    )
}
