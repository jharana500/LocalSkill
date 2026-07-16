package com.example.localskill.view.auth.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.localskill.view.theme.LocalSkillTheme
import com.example.localskill.viewmodel.LoginUiState
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun validationErrorsAreDisplayedWhenPresentInState() {
        composeTestRule.setContent {
            LocalSkillTheme {
                LoginContent(
                    uiState = LoginUiState(
                        emailError = "Email is required",
                        passwordError = "Password is required"
                    ),
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

        composeTestRule.onNodeWithText("Email is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password is required").assertIsDisplayed()
    }

    @Test
    fun togglingPasswordVisibilityInvokesCallback() {
        var toggled = false

        composeTestRule.setContent {
            LocalSkillTheme {
                LoginContent(
                    uiState = LoginUiState(password = "Str0ngPass"),
                    events = emptyFlow(),
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onPasswordVisibilityToggled = { toggled = true },
                    onSubmit = {},
                    onLoginSuccess = {},
                    onForgotPasswordClick = {},
                    onCreateAccountClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Show password").performClick()

        assert(toggled)
    }

    @Test
    fun forgotPasswordClickInvokesCallback() {
        var forgotPasswordClicked = false

        composeTestRule.setContent {
            LocalSkillTheme {
                LoginContent(
                    uiState = LoginUiState(),
                    events = emptyFlow(),
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onPasswordVisibilityToggled = {},
                    onSubmit = {},
                    onLoginSuccess = {},
                    onForgotPasswordClick = { forgotPasswordClicked = true },
                    onCreateAccountClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Forgot password?").performClick()

        assert(forgotPasswordClicked)
    }
}
