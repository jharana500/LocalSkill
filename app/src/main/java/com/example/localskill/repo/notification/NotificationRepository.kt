package com.example.localskill.repo.notification

import com.example.localskill.model.NotificationModel
import com.example.localskill.utils.Resource

interface NotificationRepository {
    fun createNotification(notification: NotificationModel, callback: (Resource<Unit>) -> Unit)
    fun getUserNotifications(userId: String, callback: (Resource<List<NotificationModel>>) -> Unit)
    fun markNotificationAsRead(userId: String, notificationId: String, callback: (Resource<Unit>) -> Unit)
    fun markAllNotificationsAsRead(userId: String, callback: (Resource<Unit>) -> Unit)
    fun deleteNotification(userId: String, notificationId: String, callback: (Resource<Unit>) -> Unit)
    fun saveFcmToken(userId: String, token: String, callback: (Resource<Unit>) -> Unit)
}
