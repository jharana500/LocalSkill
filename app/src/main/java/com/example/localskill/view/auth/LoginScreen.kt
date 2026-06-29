package com.example.localskill.view.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.localskill.viewmodel.auth.LoginUiState
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.LocalSkillButton
import com.example.localskill.view.components.LocalSkillPasswordField
import com.example.localskill.view.components.LocalSkillTextField

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Welcome back", style = MaterialTheme.typography.headlineMedium)
        Text("Login to find work or hire nearby talent.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        LocalSkillTextField(state.email, onEmailChange, "Email", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        LocalSkillPasswordField(state.password, onPasswordChange, "Password")
        Spacer(Modifier.height(8.dp))
        Text("Forgot password?", color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.End).clickable(onClick = onForgotPassword))
        Spacer(Modifier.height(16.dp))
        ErrorMessage(state.errorMessage)
        Spacer(Modifier.height(16.dp))
        LocalSkillButton("Login", state.isLoading, onLogin)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("New to LocalSkill? ")
            Text("Create account", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onRegister))
        }
    }
}
