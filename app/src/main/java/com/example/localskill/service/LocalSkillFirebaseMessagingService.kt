package com.example.localskill.service

import com.example.localskill.repo.notification.NotificationRepositoryImpl
import com.example.localskill.utils.NotificationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class LocalSkillFirebaseMessagingService : FirebaseMessagingService() {
    private val notificationRepository = NotificationRepositoryImpl()

    override fun onNewToken(token: String) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            notificationRepository.saveFcmToken(userId, token) { }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "LocalSkill"
        val body = message.notification?.body ?: message.data["message"] ?: "You have a new update."
        NotificationUtils.showNotification(this, title, body)
    }
}
