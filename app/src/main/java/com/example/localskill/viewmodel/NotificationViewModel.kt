package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.NotificationModel
import com.example.localskill.model.UserRole
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.NotificationRepo
import com.example.localskill.utils.ResultState
import com.example.localskill.view.navigation.NotificationDestinationMapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationModel> = emptyList(),
    val unreadCount: Int = 0,
    val errorMessage: String? = null
)

sealed interface NotificationEvent {
    data class Navigate(val route: String) : NotificationEvent
    data class ShowMessage(val message: String) : NotificationEvent
}

class NotificationViewModel(
    private val authRepo: AuthRepo,
    private val notificationRepo: NotificationRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val eventsChannel = Channel<NotificationEvent>(Channel.BUFFERED)
    val events = eventsChannel.receiveAsFlow()

    val unreadCount: StateFlow<Int> = notificationRepo.observeUnreadCount(authRepo.currentUserId().orEmpty())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResultState.Success(0))
        .let { unreadState ->
            MutableStateFlow(0).also { countState ->
                viewModelScope.launch {
                    unreadState.collect { result ->
                        if (result is ResultState.Success) countState.value = result.data
                    }
                }
            }
        }

    init {
        refresh()
    }

    fun refresh() {
        val userId = authRepo.currentUserId().orEmpty()
        if (userId.isBlank()) {
            _uiState.value = NotificationUiState(errorMessage = "Sign in again to view notifications.")
            return
        }
        viewModelScope.launch {
            notificationRepo.observeNotifications(userId).collect { result ->
                when (result) {
                    ResultState.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is ResultState.Success -> _uiState.value = NotificationUiState(
                        notifications = result.data,
                        unreadCount = result.data.count { !it.isRead }
                    )

                    is ResultState.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }

                    ResultState.Idle -> Unit
                }
            }
        }
    }

    fun markRead(notificationId: String) {
        val userId = authRepo.currentUserId().orEmpty()
        viewModelScope.launch {
            when (val result = notificationRepo.markAsRead(userId, notificationId)) {
                is ResultState.Error -> eventsChannel.send(NotificationEvent.ShowMessage(result.message))
                else -> Unit
            }
        }
    }

    fun markAllRead() {
        val userId = authRepo.currentUserId().orEmpty()
        viewModelScope.launch {
            when (val result = notificationRepo.markAllAsRead(userId)) {
                is ResultState.Error -> eventsChannel.send(NotificationEvent.ShowMessage(result.message))
                else -> Unit
            }
        }
    }

    fun openNotification(notification: NotificationModel, role: UserRole) {
        val userId = authRepo.currentUserId().orEmpty()
        if (userId.isBlank() || notification.recipientId != userId) {
            viewModelScope.launch { eventsChannel.send(NotificationEvent.ShowMessage("This notification is no longer available.")) }
            return
        }
        val route = NotificationDestinationMapper.routeFor(notification, role)
        viewModelScope.launch {
            if (route == null) {
                eventsChannel.send(NotificationEvent.ShowMessage("This notification target is unavailable."))
                return@launch
            }
            notificationRepo.markAsRead(userId, notification.id)
            eventsChannel.send(NotificationEvent.Navigate(route))
        }
    }
}
