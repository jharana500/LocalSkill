package com.example.localskill.viewmodel.notification

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.NotificationModel
import com.example.localskill.repo.auth.AuthRepository
import com.example.localskill.repo.auth.AuthRepositoryImpl
import com.example.localskill.repo.notification.NotificationRepository
import com.example.localskill.repo.notification.NotificationRepositoryImpl
import com.example.localskill.utils.Resource

data class NotificationsUiState(
    val notifications: List<NotificationModel> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class NotificationsViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(NotificationsUiState())
        private set

    fun load() {
        val userId = authRepository.currentUserId()
        if (userId == null) {
            uiState = uiState.copy(errorMessage = "Session expired. Please login again")
            return
        }
        notificationRepository.getUserNotifications(userId) { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true, errorMessage = null)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> uiState.withNotifications(result.data).copy(isLoading = false)
            }
        }
    }

    fun markAsRead(notification: NotificationModel) {
        val userId = authRepository.currentUserId() ?: return
        notificationRepository.markNotificationAsRead(userId, notification.id) { result ->
            when (result) {
                Resource.Loading -> Unit
                is Resource.Error -> uiState = uiState.copy(errorMessage = result.message)
                is Resource.Success -> load()
            }
        }
    }

    fun markAllAsRead() {
        val userId = authRepository.currentUserId() ?: return
        notificationRepository.markAllNotificationsAsRead(userId) { result ->
            when (result) {
                Resource.Loading -> Unit
                is Resource.Error -> uiState = uiState.copy(errorMessage = result.message)
                is Resource.Success -> load()
            }
        }
    }

    fun delete(notification: NotificationModel) {
        val userId = authRepository.currentUserId() ?: return
        notificationRepository.deleteNotification(userId, notification.id) { result ->
            when (result) {
                Resource.Loading -> Unit
                is Resource.Error -> uiState = uiState.copy(errorMessage = result.message)
                is Resource.Success -> load()
            }
        }
    }
}

private fun NotificationsUiState.withNotifications(notifications: List<NotificationModel>): NotificationsUiState =
    copy(
        notifications = notifications,
        unreadCount = notifications.count { !it.isRead }
    )
