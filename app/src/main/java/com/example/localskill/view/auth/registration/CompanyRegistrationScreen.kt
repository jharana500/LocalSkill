package com.example.localskill.view.auth.registration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.utils.ValidationUtils
import com.example.localskill.view.common.components.LocalSkillCheckbox
import com.example.localskill.view.common.components.LocalSkillPasswordField
import com.example.localskill.view.common.components.LocalSkillPhoneField
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.components.PasswordStrengthIndicator
import com.example.localskill.view.common.states.ErrorMessage
import com.example.localskill.view.theme.LocalSkillTheme
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.AuthEvent
import com.example.localskill.viewmodel.AuthViewModel
import com.example.localskill.viewmodel.CompanyRegistrationUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun CompanyRegistrationScreen(
    viewModel: AuthViewModel,
    onRegistrationSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.companyRegistrationUiState.collectAsStateWithLifecycle()

    CompanyRegistrationContent(
        uiState = uiState,
        events = viewModel.events,
        onCompanyNameChanged = viewModel::onCompanyNameChanged,
        onContactPersonNameChanged = viewModel::onCompanyContactPersonNameChanged,
        onEmailChanged = viewModel::onCompanyEmailChanged,
        onPhoneChanged = viewModel::onCompanyPhoneChanged,
        onAddressChanged = viewModel::onCompanyAddressChanged,
        onPasswordChanged = viewModel::onCompanyPasswordChanged,
        onConfirmPasswordChanged = viewModel::onCompanyConfirmPasswordChanged,
        onPasswordVisibilityToggled = viewModel::onCompanyPasswordVisibilityToggled,
        onConfirmPasswordVisibilityToggled = viewModel::onCompanyConfirmPasswordVisibilityToggled,
        onTermsAcceptedChanged = viewModel::onCompanyTermsAcceptedChanged,
        onSubmit = viewModel::submitCompanyRegistration,
        onRegistrationSuccess = onRegistrationSuccess,
        onBack = onBack
    )
}

@Composable
internal fun CompanyRegistrationContent(
    uiState: CompanyRegistrationUiState,
    events: Flow<AuthEvent>,
    onCompanyNameChanged: (String) -> Unit,
    onContactPersonNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onPasswordVisibilityToggled: () -> Unit,
    onConfirmPasswordVisibilityToggled: () -> Unit,
    onTermsAcceptedChanged: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                is AuthEvent.CompanyRegistrationSuccess -> onRegistrationSuccess()
                is AuthEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Company Account", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            Text(
                text = "Register your company",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your account will be reviewed before you can post jobs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xxs, bottom = Spacing.lg)
            )

            LocalSkillTextField(
                value = uiState.companyName,
                onValueChange = onCompanyNameChanged,
                label = "Company name",
                errorMessage = uiState.companyNameError,
                enabled = !uiState.isLoading,
                contentType = ContentType.Username
            )

            LocalSkillTextField(
                value = uiState.contactPersonName,
                onValueChange = onContactPersonNameChanged,
                label = "Contact person full name",
                errorMessage = uiState.contactPersonNameError,
                enabled = !uiState.isLoading,
                contentType = ContentType.PersonFullName,
                modifier = Modifier.padding(top = Spacing.md)
            )

            LocalSkillTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                label = "Company email",
                errorMessage = uiState.emailError,
                enabled = !uiState.isLoading,
                keyboardType = KeyboardType.Email,
                capitalization = KeyboardCapitalization.None,
                contentType = ContentType.EmailAddress,
                modifier = Modifier.padding(top = Spacing.md)
            )

            LocalSkillPhoneField(
                value = uiState.phone,
                onValueChange = onPhoneChanged,
                errorMessage = uiState.phoneError,
                enabled = !uiState.isLoading,
                modifier = Modifier.padding(top = Spacing.md)
            )

            LocalSkillTextField(
                value = uiState.address,
                onValueChange = onAddressChanged,
                label = "City or address",
                errorMessage = uiState.addressError,
                enabled = !uiState.isLoading,
                contentType = ContentType.AddressRegion,
                modifier = Modifier.padding(top = Spacing.md)
            )

            LocalSkillPasswordField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                label = "Password",
                isVisible = uiState.isPasswordVisible,
                onVisibilityToggle = onPasswordVisibilityToggled,
                errorMessage = uiState.passwordError,
                enabled = !uiState.isLoading,
                contentType = ContentType.NewPassword,
                modifier = Modifier.padding(top = Spacing.md)
            )
            PasswordStrengthIndicator(
                strength = ValidationUtils.passwordStrength(uiState.password),
                modifier = Modifier.padding(top = Spacing.xxs)
            )

            LocalSkillPasswordField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChanged,
                label = "Confirm password",
                isVisible = uiState.isConfirmPasswordVisible,
                onVisibilityToggle = onConfirmPasswordVisibilityToggled,
                errorMessage = uiState.confirmPasswordError,
                enabled = !uiState.isLoading,
                imeAction = ImeAction.Done,
                contentType = ContentType.NewPassword,
                modifier = Modifier.padding(top = Spacing.md)
            )

            LocalSkillCheckbox(
                checked = uiState.termsAccepted,
                onCheckedChange = onTermsAcceptedChanged,
                label = "I accept the Terms of Service and Privacy Policy",
                errorMessage = uiState.termsError,
                modifier = Modifier.padding(top = Spacing.sm)
            )

            if (uiState.errorMessage != null) {
                ErrorMessage(
                    message = uiState.errorMessage,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
            }

            LocalSkillPrimaryButton(
                text = "Create company account",
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

@Preview(showBackground = true)
@Composable
private fun CompanyRegistrationScreenPreview() {
    LocalSkillTheme {
        CompanyRegistrationContent(
            uiState = CompanyRegistrationUiState(),
            events = emptyFlow(),
            onCompanyNameChanged = {},
            onContactPersonNameChanged = {},
            onEmailChanged = {},
            onPhoneChanged = {},
            onAddressChanged = {},
            onPasswordChanged = {},
            onConfirmPasswordChanged = {},
            onPasswordVisibilityToggled = {},
            onConfirmPasswordVisibilityToggled = {},
            onTermsAcceptedChanged = {},
            onSubmit = {},
            onRegistrationSuccess = {},
            onBack = {}
        )
    }
}
