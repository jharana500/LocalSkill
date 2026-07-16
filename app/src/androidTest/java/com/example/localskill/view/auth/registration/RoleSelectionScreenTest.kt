package com.example.localskill.view.auth.registration

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.localskill.view.theme.LocalSkillTheme
import org.junit.Rule
import org.junit.Test

class RoleSelectionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tappingJobSeekerCardTriggersCallback() {
        var jobSeekerSelected = false

        composeTestRule.setContent {
            LocalSkillTheme {
                RoleSelectionScreen(
                    onJobSeekerSelected = { jobSeekerSelected = true },
                    onCompanySelected = {},
                    onLoginClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Job Seeker").performClick()

        assert(jobSeekerSelected)
    }

    @Test
    fun tappingCompanyCardTriggersCallback() {
        var companySelected = false

        composeTestRule.setContent {
            LocalSkillTheme {
                RoleSelectionScreen(
                    onJobSeekerSelected = {},
                    onCompanySelected = { companySelected = true },
                    onLoginClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Company").performClick()

        assert(companySelected)
    }

    @Test
    fun tappingLogInTriggersCallback() {
        var loginClicked = false

        composeTestRule.setContent {
            LocalSkillTheme {
                RoleSelectionScreen(
                    onJobSeekerSelected = {},
                    onCompanySelected = {},
                    onLoginClick = { loginClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Log in").performClick()

        assert(loginClicked)
    }
}
