package com.example.localskill.view.admin.scaffold

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.localskill.view.navigation.AdminRoute
import com.example.localskill.view.theme.LocalSkillTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AdminBottomBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickingCompaniesNavigatesToTheCompaniesRoute() {
        var navigatedTo: String? = null

        composeTestRule.setContent {
            LocalSkillTheme {
                AdminBottomBar(
                    currentRoute = AdminRoute.Dashboard.route,
                    onNavigate = { navigatedTo = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Companies").performClick()

        assertEquals(AdminRoute.Companies.route, navigatedTo)
    }

    @Test
    fun clickingMoreNavigatesToTheMoreRoute() {
        var navigatedTo: String? = null

        composeTestRule.setContent {
            LocalSkillTheme {
                AdminBottomBar(
                    currentRoute = AdminRoute.Dashboard.route,
                    onNavigate = { navigatedTo = it }
                )
            }
        }

        composeTestRule.onNodeWithText("More").performClick()

        assertEquals(AdminRoute.More.route, navigatedTo)
    }

    @Test
    fun currentTabIsShownAsSelected() {
        composeTestRule.setContent {
            LocalSkillTheme {
                AdminBottomBar(
                    currentRoute = AdminRoute.Reports.route,
                    onNavigate = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Reports").assertExists()
    }
}
