package com.example.localskill.model

data class ReviewModel(
    val id: String = "",
    val jobId: String = "",
    val applicationId: String = "",
    val reviewerId: String = "",
    val reviewerName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val reviewerRole: String = "",
    val receiverRole: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val jobTitle: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
