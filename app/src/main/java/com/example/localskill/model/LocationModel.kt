package com.example.localskill.model

data class LocationModel(
    val address: String = "",
    val city: String = "",
    val area: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)
