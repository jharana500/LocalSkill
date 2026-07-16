package com.example.localskill.view.auth.passwordreset

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillTextButton
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.theme.LocalSkillTheme
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.AuthViewModel
import com.example.localskill.viewmodel.PasswordResetUiState

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.passwordResetUiState.collectAsStateWithLifecycle()

    ForgotPasswordContent(
        uiState = uiState,
        onEmailChanged = viewModel::onPasswordResetEmailChanged,
        onSubmit = viewModel::submitPasswordReset,
        onBack = onBack
    )
}

@Composable
private fun ForgotPasswordContent(
    uiState: PasswordResetUiState,
    onEmailChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Reset Password", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg)
        ) {
            if (uiState.isSubmitted) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = Spacing.xxl)
                            .size(48.dp)
                    )
                    Text(
                        text = "Check your email",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = Spacing.md)
                    )
                    Text(
                        text = "If an account exists for ${uiState.email}, we've sent a link to reset your password.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                    LocalSkillTextButton(
                        text = "Back to login",
                        onClick = onBack,
                        modifier = Modifier.padding(top = Spacing.lg)
                    )
                }
            } else {
                Text(
                    text = "Forgot your password?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Enter the email associated with your account and we'll send you a reset link.",
                    style = MaterialTheme.typography.bodyMedium,
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
                    contentType = ContentType.EmailAddress
                )

                if (uiState.errorMessage != null) {
                    ErrorMessage(
                        message = uiState.errorMessage,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                }

                LocalSkillPrimaryButton(
                    text = "Send reset link",
                    onClick = {
                        focusManager.clearFocus()
                        onSubmit()
                    },
                    isLoading = uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.lg)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    LocalSkillTheme {
        ForgotPasswordContent(
            uiState = PasswordResetUiState(),
            onEmailChanged = {},
            onSubmit = {},
            onBack = {}
        )
    }
}
