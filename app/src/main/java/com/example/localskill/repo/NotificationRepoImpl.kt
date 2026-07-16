package com.example.localskill.repo

import com.example.localskill.model.NotificationEntityType
import com.example.localskill.model.NotificationModel
import com.example.localskill.model.NotificationType
import com.example.localskill.utils.Constants
import com.example.localskill.utils.FirebaseErrorMapper
import com.example.localskill.utils.ResultState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class NotificationRepoImpl(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : NotificationRepo {

    private val notificationsRef: DatabaseReference = database.getReference(Constants.NOTIFICATIONS_NODE)

    override fun observeNotifications(userId: String): Flow<ResultState<List<NotificationModel>>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(ResultState.Error("Sign in again to view notifications."))
            close()
            return@callbackFlow
        }
        trySend(ResultState.Loading)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notifications = snapshot.children.mapNotNull { it.getValue(NotificationModel::class.java) }
                    .filter { it.recipientId == userId }
                    .sortedByDescending { it.createdAt }
                trySend(ResultState.Success(notifications))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(ResultState.Error(FirebaseErrorMapper.map(error.toException()), error.toException()))
            }
        }
        notificationsRef.child(userId).addValueEventListener(listener)
        awaitClose { notificationsRef.child(userId).removeEventListener(listener) }
    }

    override fun observeUnreadCount(userId: String): Flow<ResultState<Int>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(ResultState.Error("Sign in again to view notifications."))
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.children.count {
                    val notification = it.getValue(NotificationModel::class.java)
                    notification?.recipientId == userId && !notification.isRead
                }
                trySend(ResultState.Success(count))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(ResultState.Error(FirebaseErrorMapper.map(error.toException()), error.toException()))
            }
        }
        notificationsRef.child(userId).addValueEventListener(listener)
        awaitClose { notificationsRef.child(userId).removeEventListener(listener) }
    }

    override suspend fun getNotifications(userId: String): ResultState<List<NotificationModel>> {
        if (userId.isBlank()) return ResultState.Error("Sign in again to view notifications.")
        return try {
            val snapshot = notificationsRef.child(userId).get().await()
            val notifications = snapshot.children.mapNotNull { it.getValue(NotificationModel::class.java) }
                .filter { it.recipientId == userId }
                .sortedByDescending { it.createdAt }
            ResultState.Success(notifications)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun markAsRead(userId: String, notificationId: String): ResultState<Unit> {
        if (userId.isBlank() || notificationId.isBlank()) return ResultState.Error("Notification not found.")
        return try {
            val ref = notificationsRef.child(userId).child(notificationId)
            val current = ref.get().await().getValue(NotificationModel::class.java)
                ?: return ResultState.Error("Notification not found.")
            if (current.recipientId != userId) return ResultState.Error("You cannot update this notification.")
            if (!current.isRead) ref.child("isRead").setValue(true).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun markAllAsRead(userId: String): ResultState<Unit> {
        if (userId.isBlank()) return ResultState.Error("Sign in again to update notifications.")
        return try {
            val snapshot = notificationsRef.child(userId).get().await()
            val updates = snapshot.children.mapNotNull { child ->
                val notification = child.getValue(NotificationModel::class.java)
                if (notification?.recipientId == userId && !notification.isRead) {
                    "${Constants.NOTIFICATIONS_NODE}/$userId/${child.key}/isRead" to true
                } else {
                    null
                }
            }.toMap()
            if (updates.isNotEmpty()) database.reference.updateChildren(updates).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun createNotificationIfAbsent(
        recipientId: String,
        senderId: String,
        type: NotificationType,
        relatedEntityType: NotificationEntityType,
        relatedEntityId: String,
        secondaryEntityId: String,
        eventKey: String
    ): ResultState<NotificationModel> {
        if (recipientId.isBlank() || relatedEntityId.isBlank() || eventKey.isBlank()) {
            return ResultState.Error("Notification target is unavailable.")
        }
        val notificationId = deterministicId(eventKey)
        val notificationRef = notificationsRef.child(recipientId).child(notificationId)
        return try {
            val existing = notificationRef.get().await().getValue(NotificationModel::class.java)
            if (existing != null) return ResultState.Success(existing)

            val now = System.currentTimeMillis()
            val notification = NotificationModel(
                id = notificationId,
                recipientId = recipientId,
                senderId = senderId,
                type = type.name,
                relatedEntityType = relatedEntityType.name,
                relatedEntityId = relatedEntityId,
                secondaryEntityId = secondaryEntityId,
                isRead = false,
                createdAt = now
            )
            notificationRef.setValue(notification.toMap()).await()
            ResultState.Success(notification)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    private fun deterministicId(eventKey: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(eventKey.toByteArray())
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }.take(40)
    }
}
