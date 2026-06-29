package com.example.localskill.view.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.localskill.model.UserRole
import com.example.localskill.viewmodel.auth.RoleSelectionUiState
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.LocalSkillButton
import com.example.localskill.view.components.RoleCard

@Composable
fun RoleSelectionScreen(state: RoleSelectionUiState, onSelect: (UserRole) -> Unit, onContinue: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(Modifier.height(60.dp))
        Text("Choose your role", style = MaterialTheme.typography.headlineMedium)
        Text("LocalSkill will shape your dashboard around this.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        RoleCard(UserRole.WORKER, state.selectedRole == UserRole.WORKER) { onSelect(UserRole.WORKER) }
        Spacer(Modifier.height(12.dp))
        RoleCard(UserRole.EMPLOYER, state.selectedRole == UserRole.EMPLOYER) { onSelect(UserRole.EMPLOYER) }
        Spacer(Modifier.height(16.dp))
        ErrorMessage(state.errorMessage)
        Spacer(Modifier.height(16.dp))
        LocalSkillButton("Continue", state.isLoading, onContinue)
    }
}
