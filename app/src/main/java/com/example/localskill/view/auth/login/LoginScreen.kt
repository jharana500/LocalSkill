package com.example.localskill.view.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.view.common.components.LocalSkillLogo
import com.example.localskill.view.common.components.LocalSkillPasswordField
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillTextButton
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.theme.LocalSkillTheme
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.AuthEvent
import com.example.localskill.viewmodel.AuthViewModel
import com.example.localskill.viewmodel.LoginUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val uiState by viewModel.loginUiState.collectAsStateWithLifecycle()

    LoginContent(
        uiState = uiState,
        events = viewModel.events,
        onEmailChanged = viewModel::onLoginEmailChanged,
        onPasswordChanged = viewModel::onLoginPasswordChanged,
        onPasswordVisibilityToggled = viewModel::onLoginPasswordVisibilityToggled,
        onSubmit = viewModel::submitLogin,
        onLoginSuccess = onLoginSuccess,
        onForgotPasswordClick = onForgotPasswordClick,
        onCreateAccountClick = onCreateAccountClick
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    events: Flow<AuthEvent>,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityToggled: () -> Unit,
    onSubmit: () -> Unit,
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                is AuthEvent.LoginSuccess -> onLoginSuccess()
                is AuthEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LocalSkillLogo()

            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = Spacing.lg)
            )
            Text(
                text = "Log in to continue to LocalSkill",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xxs, bottom = Spacing.xl)
            )

            LocalSkillTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                label = "Email",
                errorMessage = uiState.emailError,
                enabled = !uiState.isLoading,
                keyboardType = KeyboardType.Email,
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Next,
                contentType = ContentType.EmailAddress
            )

            LocalSkillPasswordField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                label = "Password",
                isVisible = uiState.isPasswordVisible,
                onVisibilityToggle = onPasswordVisibilityToggled,
                errorMessage = uiState.passwordError,
                enabled = !uiState.isLoading,
                imeAction = ImeAction.Done,
                modifier = Modifier.padding(top = Spacing.md)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xxs),
                horizontalArrangement = Arrangement.End
            ) {
                LocalSkillTextButton(text = "Forgot password?", onClick = onForgotPasswordClick)
            }

            if (uiState.errorMessage != null) {
                ErrorMessage(
                    message = uiState.errorMessage,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }

            LocalSkillPrimaryButton(
                text = "Log in",
                onClick = {
                    focusManager.clearFocus()
                    onSubmit()
                },
                isLoading = uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.lg)
            )

            Row(
                modifier = Modifier.padding(top = Spacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LocalSkillTextButton(text = "Sign up", onClick = onCreateAccountClick)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    LocalSkillTheme {
        LoginContent(
            uiState = LoginUiState(),
            events = emptyFlow(),
            onEmailChanged = {},
            onPasswordChanged = {},
            onPasswordVisibilityToggled = {},
            onSubmit = {},
            onLoginSuccess = {},
            onForgotPasswordClick = {},
            onCreateAccountClick = {}
        )
    }
}
