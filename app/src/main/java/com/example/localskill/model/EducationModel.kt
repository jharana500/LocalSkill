package com.example.localskill.model

data class EducationModel(
    val id: String = "",
    val institution: String = "",
    val qualification: String = "",
    val fieldOfStudy: String = "",
    val startYear: Int = 0,
    val endYear: Int? = null,
    val currentlyStudying: Boolean = false,
    val description: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "institution" to institution,
        "qualification" to qualification,
        "fieldOfStudy" to fieldOfStudy,
        "startYear" to startYear,
        "endYear" to endYear,
        "currentlyStudying" to currentlyStudying,
        "description" to description
    )
}
