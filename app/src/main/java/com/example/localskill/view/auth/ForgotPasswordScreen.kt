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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.localskill.viewmodel.auth.ForgotPasswordUiState
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.LocalSkillButton
import com.example.localskill.view.components.LocalSkillTextField

@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(Modifier.height(80.dp))
        Text("Reset password", style = MaterialTheme.typography.headlineMedium)
        Text("Enter the email linked to your LocalSkill account.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        LocalSkillTextField(state.email, onEmailChange, "Email", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(16.dp))
        ErrorMessage(state.errorMessage)
        state.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(16.dp))
        LocalSkillButton("Send reset link", state.isLoading, onSubmit)
        Spacer(Modifier.height(12.dp))
        LocalSkillButton("Back to login", onClick = onBackToLogin)
    }
}
