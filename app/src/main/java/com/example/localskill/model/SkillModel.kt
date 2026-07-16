package com.example.localskill.model

data class SkillModel(
    val name: String = "",
    val proficiency: String = ""
) {
    val normalizedName: String
        get() = name.trim().lowercase()
}
