package com.example.localskill.repo

import com.example.localskill.model.NotificationEntityType
import com.example.localskill.model.NotificationModel
import com.example.localskill.model.NotificationType
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface NotificationRepo {
    fun observeNotifications(userId: String): Flow<ResultState<List<NotificationModel>>>

    fun observeUnreadCount(userId: String): Flow<ResultState<Int>>

    suspend fun getNotifications(userId: String): ResultState<List<NotificationModel>>

    suspend fun markAsRead(userId: String, notificationId: String): ResultState<Unit>

    suspend fun markAllAsRead(userId: String): ResultState<Unit>

    suspend fun createNotificationIfAbsent(
        recipientId: String,
        senderId: String,
        type: NotificationType,
        relatedEntityType: NotificationEntityType,
        relatedEntityId: String,
        secondaryEntityId: String = "",
        eventKey: String
    ): ResultState<NotificationModel>
}
