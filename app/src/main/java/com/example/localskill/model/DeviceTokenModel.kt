package com.example.localskill.model

data class DeviceTokenModel(
    val deviceId: String = "",
    val token: String = "",
    val platform: String = "android",
    val active: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "deviceId" to deviceId,
        "token" to token,
        "platform" to platform,
        "active" to active,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}
