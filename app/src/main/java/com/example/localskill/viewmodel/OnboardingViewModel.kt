package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.repo.AppPreferencesRepo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(private val appPreferencesRepo: AppPreferencesRepo) : ViewModel() {

    private val _completedEvent = Channel<Unit>(Channel.BUFFERED)
    val completedEvent: Flow<Unit> = _completedEvent.receiveAsFlow()

    fun completeOnboarding() {
        viewModelScope.launch {
            appPreferencesRepo.setOnboardingCompleted(true)
            _completedEvent.send(Unit)
        }
    }
}
