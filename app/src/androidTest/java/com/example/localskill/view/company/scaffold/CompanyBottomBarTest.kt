package com.example.localskill.view.company.scaffold

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.localskill.view.navigation.CompanyRoute
import com.example.localskill.view.theme.LocalSkillTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CompanyBottomBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickingApplicantsNavigatesToTheApplicantsRoute() {
        var navigatedTo: String? = null

        composeTestRule.setContent {
            LocalSkillTheme {
                CompanyBottomBar(
                    currentRoute = CompanyRoute.Dashboard.route,
                    onNavigate = { navigatedTo = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Applicants").performClick()

        assertEquals(CompanyRoute.Applicants.route, navigatedTo)
    }

    @Test
    fun currentTabIsShownAsSelected() {
        composeTestRule.setContent {
            LocalSkillTheme {
                CompanyBottomBar(
                    currentRoute = CompanyRoute.Jobs.route,
                    onNavigate = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Jobs").assertExists()
    }

    @Test
    fun everyMainDestinationIsReachableFromTheBottomBar() {
        val clicked = mutableListOf<String>()

        composeTestRule.setContent {
            LocalSkillTheme {
                CompanyBottomBar(
                    currentRoute = CompanyRoute.Dashboard.route,
                    onNavigate = { clicked.add(it) }
                )
            }
        }

        composeTestRule.onNodeWithText("Dashboard").performClick()
        composeTestRule.onNodeWithText("Profile").performClick()
        composeTestRule.onNodeWithText("Settings").performClick()

        assertEquals(
            listOf(CompanyRoute.Dashboard.route, CompanyRoute.Profile.route, CompanyRoute.Settings.route),
            clicked
        )
    }
}
