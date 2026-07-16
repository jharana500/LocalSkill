package com.example.localskill.view.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.localskill.view.auth.registration.RoleSelectionScreen
import com.example.localskill.view.theme.LocalSkillTheme
import org.junit.Rule
import org.junit.Test

/**
 * Exercises real back-stack mechanics (forward navigation + system back)
 * against the production RoleSelectionScreen, without pulling in
 * AppSessionViewModel/Firebase — those are covered separately by
 * AppSessionViewModelTest's routing-decision unit tests.
 */
class RoleSelectionBackStackTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun backButtonReturnsFromRegistrationToRoleSelection() {
        composeTestRule.setContent {
            LocalSkillTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "role_selection") {
                    composable("role_selection") {
                        RoleSelectionScreen(
                            onJobSeekerSelected = { navController.navigate("job_seeker_registration") },
                            onCompanySelected = {},
                            onLoginClick = {}
                        )
                    }
                    composable("job_seeker_registration") {
                        Text("Job Seeker Registration Stub")
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Join LocalSkill").assertExists()

        composeTestRule.onNodeWithText("Job Seeker").performClick()
        composeTestRule.onNodeWithText("Job Seeker Registration Stub").assertExists()

        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        composeTestRule.onNodeWithText("Join LocalSkill").assertExists()
    }
}
