package com.example.localskill.view.auth.registration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.localskill.view.theme.LocalSkillTheme
import com.example.localskill.viewmodel.JobSeekerRegistrationUiState
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test

class JobSeekerRegistrationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun validationErrorsAreDisplayedWhenPresentInState() {
        composeTestRule.setContent {
            LocalSkillTheme {
                JobSeekerRegistrationContent(
                    uiState = JobSeekerRegistrationUiState(
                        fullNameError = "Full name is required",
                        emailError = "Email is required",
                        phoneError = "Phone number is required",
                        addressError = "City or address is required",
                        passwordError = "Password is required",
                        termsError = "You must accept the Terms and Privacy Policy"
                    ),
                    events = emptyFlow(),
                    onFullNameChanged = {},
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

        composeTestRule.onNodeWithText("Full name is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone number is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("City or address is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("You must accept the Terms and Privacy Policy").assertIsDisplayed()
    }
}
