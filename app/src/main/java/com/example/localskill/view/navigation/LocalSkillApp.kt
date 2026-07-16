package com.example.localskill.view.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.localskill.rememberAppContainer
import com.example.localskill.view.theme.LocalSkillTheme
import com.example.localskill.viewmodel.AppSessionViewModel
import com.example.localskill.viewmodel.LocalSkillViewModelFactory

@Composable
fun LocalSkillApp() {
    val appContainer = rememberAppContainer()
    val factory = LocalSkillViewModelFactory(appContainer)
    val sessionViewModel: AppSessionViewModel = viewModel(factory = factory)

    val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()
    val appTheme by sessionViewModel.appTheme.collectAsStateWithLifecycle()

    LocalSkillTheme(appTheme = appTheme, activeRole = sessionUiState.activeRole) {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavGraph(sessionViewModel = sessionViewModel, viewModelFactory = factory)
        }
    }
}
