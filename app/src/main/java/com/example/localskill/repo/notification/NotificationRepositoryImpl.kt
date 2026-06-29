package com.example.localskill.repo.notification

import com.example.localskill.model.NotificationModel
import com.example.localskill.utils.Constants
import com.example.localskill.utils.Resource
import com.example.localskill.utils.readableMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class NotificationRepositoryImpl(
    database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : NotificationRepository {
    private val notificationsRef = database.getReference(Constants.NOTIFICATIONS)
    private val userNotificationsRef = database.getReference(Constants.USER_NOTIFICATIONS)
    private val fcmTokensRef = database.getReference(Constants.USER_FCM_TOKENS)

    override fun createNotification(notification: NotificationModel, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        val id = notification.id.ifBlank { notificationsRef.push().key ?: UUID.randomUUID().toString() }
        if (notification.receiverId.isBlank() || id.isBlank()) {
            callback(Resource.Error("Unable to create notification"))
            return
        }
        val saved = notification.copy(id = id, isRead = false, createdAt = System.currentTimeMillis())
        val updates = mapOf<String, Any>(
            "${Constants.NOTIFICATIONS}/$id" to saved,
            "${Constants.USER_NOTIFICATIONS}/${notification.receiverId}/$id" to saved
        )
        notificationsRef.root.updateChildren(updates)
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun getUserNotifications(userId: String, callback: (Resource<List<NotificationModel>>) -> Unit) {
        callback(Resource.Loading)
        userNotificationsRef.child(userId).get()
            .addOnSuccessListener { callback(Resource.Success(it.toNotificationList())) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun markNotificationAsRead(userId: String, notificationId: String, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        updateNotificationFields(userId, notificationId, mapOf("isRead" to true), callback)
    }

    override fun markAllNotificationsAsRead(userId: String, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        userNotificationsRef.child(userId).get()
            .addOnSuccessListener { snapshot ->
                val updates = mutableMapOf<String, Any>()
                snapshot.children.forEach { child ->
                    val id = child.key.orEmpty()
                    if (id.isNotBlank()) {
                        updates["${Constants.USER_NOTIFICATIONS}/$userId/$id/isRead"] = true
                        updates["${Constants.NOTIFICATIONS}/$id/isRead"] = true
                    }
                }
                if (updates.isEmpty()) callback(Resource.Success(Unit))
                else notificationsRef.root.updateChildren(updates)
                    .addOnSuccessListener { callback(Resource.Success(Unit)) }
                    .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
            }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun deleteNotification(userId: String, notificationId: String, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        val updates = mapOf<String, Any?>(
            "${Constants.USER_NOTIFICATIONS}/$userId/$notificationId" to null,
            "${Constants.NOTIFICATIONS}/$notificationId" to null
        )
        notificationsRef.root.updateChildren(updates)
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun saveFcmToken(userId: String, token: String, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        if (userId.isBlank() || token.isBlank()) {
            callback(Resource.Error("Unable to save notification token"))
            return
        }
        fcmTokensRef.child(userId).child("token").setValue(token)
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    private fun updateNotificationFields(
        userId: String,
        notificationId: String,
        fields: Map<String, Any>,
        callback: (Resource<Unit>) -> Unit
    ) {
        val updates = fields.flatMap { (field, value) ->
            listOf(
                "${Constants.USER_NOTIFICATIONS}/$userId/$notificationId/$field" to value,
                "${Constants.NOTIFICATIONS}/$notificationId/$field" to value
            )
        }.toMap()
        notificationsRef.root.updateChildren(updates)
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }
}

private fun DataSnapshot.toNotificationList(): List<NotificationModel> =
    children.mapNotNull { it.getValue(NotificationModel::class.java) }
        .sortedByDescending { it.createdAt }
