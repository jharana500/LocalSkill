package com.example.localskill.view.onboarding

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.localskill.view.theme.LocalSkillTheme
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun skipOnFirstPageFinishesOnboarding() {
        var finished = false

        composeTestRule.setContent {
            LocalSkillTheme {
                OnboardingScreen(onFinished = { finished = true })
            }
        }

        composeTestRule.onNodeWithText("Discover Local Opportunities").assertExists()
        composeTestRule.onNodeWithText("Skip").performClick()

        assert(finished)
    }

    @Test
    fun tappingNextAdvancesToTheSecondPage() {
        composeTestRule.setContent {
            LocalSkillTheme {
                OnboardingScreen(onFinished = {})
            }
        }

        composeTestRule.onNodeWithText("Next").performClick()

        composeTestRule.onNodeWithText("Apply Directly").assertExists()
    }

    @Test
    fun getStartedOnLastPageFinishesOnboarding() {
        var finished = false

        composeTestRule.setContent {
            LocalSkillTheme {
                OnboardingScreen(onFinished = { finished = true })
            }
        }

        composeTestRule.onNodeWithText("Next").performClick()
        composeTestRule.onNodeWithText("Next").performClick()

        composeTestRule.onNodeWithText("Hire and Grow").assertExists()
        composeTestRule.onNodeWithText("Get Started").performClick()

        assert(finished)
    }
}
