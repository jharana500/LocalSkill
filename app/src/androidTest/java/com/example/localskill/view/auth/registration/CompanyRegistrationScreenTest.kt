package com.example.localskill.view.auth.registration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.localskill.view.theme.LocalSkillTheme
import com.example.localskill.viewmodel.CompanyRegistrationUiState
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test

class CompanyRegistrationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun validationErrorsAreDisplayedWhenPresentInState() {
        composeTestRule.setContent {
            LocalSkillTheme {
                CompanyRegistrationContent(
                    uiState = CompanyRegistrationUiState(
                        companyNameError = "Company name is required",
                        contactPersonNameError = "Full name is required",
                        emailError = "Email is required",
                        termsError = "You must accept the Terms and Privacy Policy"
                    ),
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

        composeTestRule.onNodeWithText("Company name is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Full name is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("You must accept the Terms and Privacy Policy").assertIsDisplayed()
    }
}
