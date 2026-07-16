package com.example.localskill.repo

import com.example.localskill.utils.ResultState

interface DeviceTokenRepo {
    suspend fun upsertCurrentDeviceToken(userId: String, deviceId: String, token: String): ResultState<Unit>

    suspend fun deactivateCurrentDeviceToken(userId: String, deviceId: String): ResultState<Unit>
}
