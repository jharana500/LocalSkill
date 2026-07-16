package com.example.localskill.repo

import com.example.localskill.model.DeviceTokenModel
import com.example.localskill.utils.Constants
import com.example.localskill.utils.FirebaseErrorMapper
import com.example.localskill.utils.ResultState
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class DeviceTokenRepoImpl(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : DeviceTokenRepo {

    private val devicesRef = database.getReference(Constants.USER_DEVICES_NODE)

    override suspend fun upsertCurrentDeviceToken(userId: String, deviceId: String, token: String): ResultState<Unit> {
        if (userId.isBlank() || deviceId.isBlank() || token.isBlank()) return ResultState.Error("Unable to register this device.")
        return try {
            val ref = devicesRef.child(userId).child(deviceId)
            val existingCreatedAt =
                ref.child("createdAt").get().await().getValue(Long::class.java) ?: System.currentTimeMillis()
            val now = System.currentTimeMillis()
            val model = DeviceTokenModel(
                deviceId = deviceId,
                token = token,
                active = true,
                createdAt = existingCreatedAt,
                updatedAt = now
            )
            ref.setValue(model.toMap()).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun deactivateCurrentDeviceToken(userId: String, deviceId: String): ResultState<Unit> {
        if (userId.isBlank() || deviceId.isBlank()) return ResultState.Success(Unit)
        return try {
            devicesRef.child(userId).child(deviceId).updateChildren(
                mapOf("active" to false, "updatedAt" to System.currentTimeMillis())
            ).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }
}
