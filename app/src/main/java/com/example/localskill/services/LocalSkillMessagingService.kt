package com.example.localskill.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.localskill.LocalSkillApplication
import com.example.localskill.MainActivity
import com.example.localskill.R
import com.example.localskill.model.NotificationType
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class LocalSkillMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (token.isBlank()) return
        val appContainer = (applicationContext as LocalSkillApplication).appContainer
        val userId = appContainer.authRepo.currentUserId().orEmpty()
        if (userId.isBlank()) return
        serviceScope.launch {
            val deviceId = appContainer.appPreferencesRepo.getOrCreateDeviceId()
            appContainer.deviceTokenRepo.upsertCurrentDeviceToken(userId, deviceId, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val type = NotificationType.from(message.data[KEY_TYPE].orEmpty()) ?: return
        createChannels()
        val title = titleFor(type)
        val body = bodyFor(type)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_NOTIFICATION_TYPE, type.name)
                putExtra(EXTRA_RELATED_ENTITY_ID, message.data[KEY_RELATED_ENTITY_ID].orEmpty())
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, channelFor(type))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        runCatching {
            NotificationManagerCompat.from(this).notify(notificationId(message, type), notification)
        }
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channels = listOf(
            NotificationChannel(CHANNEL_APPLICATIONS, "Applications", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CHANNEL_RECRUITMENT, "Recruitment", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CHANNEL_VERIFICATION, "Verification", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CHANNEL_MODERATION, "Platform moderation", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.createNotificationChannels(channels)
    }

    private fun channelFor(type: NotificationType): String = when (type) {
        NotificationType.APPLICATION_SUBMITTED,
        NotificationType.APPLICATION_UNDER_REVIEW,
        NotificationType.APPLICATION_SHORTLISTED,
        NotificationType.APPLICATION_HIRED,
        NotificationType.APPLICATION_REJECTED,
        NotificationType.APPLICATION_WITHDRAWN_CONFIRMATION,
        NotificationType.NEW_APPLICATION,
        NotificationType.APPLICATION_WITHDRAWN -> CHANNEL_APPLICATIONS

        NotificationType.INTERVIEW_SCHEDULED -> CHANNEL_RECRUITMENT
        NotificationType.VERIFICATION_SUBMITTED_CONFIRMATION,
        NotificationType.COMPANY_VERIFIED,
        NotificationType.COMPANY_VERIFICATION_REJECTED,
        NotificationType.NEW_VERIFICATION_REQUEST,
        NotificationType.COMPANY_VERIFICATION_RESUBMITTED -> CHANNEL_VERIFICATION

        NotificationType.JOB_MODERATED,
        NotificationType.JOB_RESTORED,
        NotificationType.NEW_JOB_REPORT,
        NotificationType.REPORT_RESOLVED,
        NotificationType.REPORT_REJECTED -> CHANNEL_MODERATION
    }

    private fun titleFor(type: NotificationType): String = when (type) {
        NotificationType.INTERVIEW_SCHEDULED -> "Interview scheduled"
        NotificationType.NEW_APPLICATION -> "New application"
        NotificationType.COMPANY_VERIFIED -> "Company verified"
        NotificationType.COMPANY_VERIFICATION_REJECTED -> "Verification rejected"
        NotificationType.NEW_VERIFICATION_REQUEST -> "New verification request"
        NotificationType.NEW_JOB_REPORT -> "New job report"
        else -> "LocalSkill update"
    }

    private fun bodyFor(type: NotificationType): String = when (type) {
        NotificationType.NEW_APPLICATION -> "A candidate applied to one of your jobs."
        NotificationType.INTERVIEW_SCHEDULED -> "Open LocalSkill to review interview details."
        NotificationType.COMPANY_VERIFIED -> "Your company can now publish jobs."
        NotificationType.COMPANY_VERIFICATION_REJECTED -> "Review feedback and resubmit when ready."
        NotificationType.NEW_VERIFICATION_REQUEST -> "A company is waiting for admin review."
        NotificationType.NEW_JOB_REPORT -> "A report is waiting for moderation."
        else -> "Open LocalSkill to view the latest update."
    }

    private fun notificationId(message: RemoteMessage, type: NotificationType): Int =
        (message.messageId ?: message.data[KEY_RELATED_ENTITY_ID] ?: type.name).hashCode().absoluteValue

    companion object {
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val EXTRA_RELATED_ENTITY_ID = "related_entity_id"
        private const val KEY_TYPE = "type"
        private const val KEY_RELATED_ENTITY_ID = "relatedEntityId"
        private const val CHANNEL_APPLICATIONS = "applications"
        private const val CHANNEL_RECRUITMENT = "recruitment"
        private const val CHANNEL_VERIFICATION = "verification"
        private const val CHANNEL_MODERATION = "moderation"
    }
}
