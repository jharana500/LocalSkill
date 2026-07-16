package com.example.localskill.view.jobseeker.scaffold

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.localskill.view.navigation.JobSeekerRoute
import com.example.localskill.view.theme.LocalSkillTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class JobSeekerBottomBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickingExploreNavigatesToTheBaseExploreRoute() {
        var navigatedTo: String? = null

        composeTestRule.setContent {
            LocalSkillTheme {
                JobSeekerBottomBar(
                    currentRoute = JobSeekerRoute.Home.route,
                    onNavigate = { navigatedTo = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Explore").performClick()

        assertEquals(JobSeekerRoute.Explore.BASE_ROUTE, navigatedTo)
    }

    @Test
    fun currentTabIsShownAsSelected() {
        composeTestRule.setContent {
            LocalSkillTheme {
                JobSeekerBottomBar(
                    currentRoute = JobSeekerRoute.Saved.route,
                    onNavigate = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Saved").assertExists()
    }

    @Test
    fun clickingApplicationsNavigatesToApplicationsRoute() {
        var navigatedTo: String? = null

        composeTestRule.setContent {
            LocalSkillTheme {
                JobSeekerBottomBar(
                    currentRoute = JobSeekerRoute.Home.route,
                    onNavigate = { navigatedTo = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Applications").performClick()

        assertEquals(JobSeekerRoute.Applications.route, navigatedTo)
    }
}
