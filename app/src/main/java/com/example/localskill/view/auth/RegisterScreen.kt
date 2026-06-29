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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.localskill.viewmodel.auth.RegisterUiState
import com.example.localskill.view.components.ErrorMessage
import com.example.localskill.view.components.LocalSkillButton
import com.example.localskill.view.components.LocalSkillPasswordField
import com.example.localskill.view.components.LocalSkillTextField

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
    onLogin: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Create account", style = MaterialTheme.typography.headlineMedium)
        Text("Start with your basic profile.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        LocalSkillTextField(state.fullName, onFullNameChange, "Full name")
        Spacer(Modifier.height(12.dp))
        LocalSkillTextField(state.email, onEmailChange, "Email", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        LocalSkillTextField(state.phone, onPhoneChange, "Phone", keyboardType = KeyboardType.Phone)
        Spacer(Modifier.height(12.dp))
        LocalSkillPasswordField(state.password, onPasswordChange, "Password")
        Spacer(Modifier.height(12.dp))
        LocalSkillPasswordField(state.confirmPassword, onConfirmPasswordChange, "Confirm password")
        Spacer(Modifier.height(16.dp))
        ErrorMessage(state.errorMessage)
        Spacer(Modifier.height(16.dp))
        LocalSkillButton("Create account", state.isLoading, onRegister)
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Already registered? ")
            Text("Login", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onLogin))
        }
    }
}
