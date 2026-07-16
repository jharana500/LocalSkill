package com.example.localskill.view.auth.verification

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillSecondaryButton
import com.example.localskill.view.common.components.LocalSkillTextButton
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.theme.LocalSkillTheme
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.AuthEvent
import com.example.localskill.viewmodel.AuthViewModel
import com.example.localskill.viewmodel.EmailVerificationUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun EmailVerificationScreen(
    viewModel: AuthViewModel,
    onVerified: () -> Unit,
    onSignOut: () -> Unit
) {
    val uiState by viewModel.emailVerificationUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.prepareEmailVerification(viewModel.currentEmailOrEmpty())
    }

    EmailVerificationContent(
        uiState = uiState,
        events = viewModel.events,
        onResend = viewModel::resendVerificationEmail,
        onCheckVerified = viewModel::checkEmailVerified,
        onVerified = onVerified,
        onSignOut = {
            viewModel.signOutFromVerification()
            onSignOut()
        }
    )
}

@Composable
private fun EmailVerificationContent(
    uiState: EmailVerificationUiState,
    events: Flow<AuthEvent>,
    onResend: () -> Unit,
    onCheckVerified: () -> Unit,
    onVerified: () -> Unit,
    onSignOut: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                is AuthEvent.EmailVerificationConfirmed -> onVerified()
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
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.MarkEmailUnread,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = Spacing.xxl)
                    .size(56.dp)
            )

            Text(
                text = "Verify your email",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = Spacing.md)
            )

            Text(
                text = "We've sent a verification link to ${uiState.email}. " +
                    "Open it, then come back and confirm below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.xs)
            )

            if (uiState.errorMessage != null) {
                ErrorMessage(
                    message = uiState.errorMessage,
                    modifier = Modifier.padding(top = Spacing.md)
                )
            }

            LocalSkillPrimaryButton(
                text = "I have verified my email",
                onClick = onCheckVerified,
                isLoading = uiState.isChecking,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xl)
            )

            LocalSkillSecondaryButton(
                text = if (uiState.resendCooldownSeconds > 0) {
                    "Resend available in ${uiState.resendCooldownSeconds}s"
                } else {
                    "Resend verification email"
                },
                onClick = onResend,
                enabled = !uiState.isResending && uiState.resendCooldownSeconds == 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm)
            )

            LocalSkillTextButton(
                text = "Sign out and use a different account",
                onClick = onSignOut,
                modifier = Modifier.padding(top = Spacing.md)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailVerificationScreenPreview() {
    LocalSkillTheme {
        EmailVerificationContent(
            uiState = EmailVerificationUiState(email = "jane@example.com"),
            events = emptyFlow(),
            onResend = {},
            onCheckVerified = {},
            onVerified = {},
            onSignOut = {}
        )
    }
}
