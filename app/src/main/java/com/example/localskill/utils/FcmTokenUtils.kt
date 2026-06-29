package com.example.localskill.utils

import com.example.localskill.repo.notification.NotificationRepository
import com.example.localskill.repo.notification.NotificationRepositoryImpl
import com.google.firebase.messaging.FirebaseMessaging

object FcmTokenUtils {
    fun saveTokenForUser(
        userId: String,
        notificationRepository: NotificationRepository = NotificationRepositoryImpl()
    ) {
        if (userId.isBlank()) return
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                notificationRepository.saveFcmToken(userId, token) { }
            }
    }
}
