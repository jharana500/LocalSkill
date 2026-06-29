package com.example.localskill.model

data class SkillModel(
    val id: String = "",
    val workerId: String = "",
    val title: String = "",
    val category: String = "",
    val rate: String = "",
    val experience: String = "",
    val location: String = "",
    val availability: String = "",
    val description: String = "",
    val portfolioUrl: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
