package com.example.localskill.view.jobseeker.application

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.view.theme.LocalSkillTheme
import com.example.localskill.viewmodel.ApplicationFilterTab
import com.example.localskill.viewmodel.ApplicationsUiState
import org.junit.Rule
import org.junit.Test

class ApplicationsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val applications = listOf(
        ApplicationModel(
            id = "1",
            jobTitle = "Android Developer",
            companyName = "Acme",
            status = ApplicationStatus.APPLIED.name
        ),
        ApplicationModel(
            id = "2",
            jobTitle = "Backend Developer",
            companyName = "Beta",
            status = ApplicationStatus.HIRED.name
        )
    )

    @Test
    fun allTabShowsEveryApplication() {
        composeTestRule.setContent {
            LocalSkillTheme {
                ApplicationsContent(
                    uiState = ApplicationsUiState(applications = applications, selectedTab = ApplicationFilterTab.ALL),
                    onSelectTab = {},
                    onApplicationClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Android Developer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Backend Developer").assertIsDisplayed()
    }

    @Test
    fun hiredTabHidesNonHiredApplications() {
        composeTestRule.setContent {
            LocalSkillTheme {
                ApplicationsContent(
                    uiState = ApplicationsUiState(applications = applications, selectedTab = ApplicationFilterTab.HIRED),
                    onSelectTab = {},
                    onApplicationClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Backend Developer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Android Developer").assertDoesNotExist()
    }

    @Test
    fun emptyFilteredTabShowsEmptyState() {
        composeTestRule.setContent {
            LocalSkillTheme {
                ApplicationsContent(
                    uiState = ApplicationsUiState(applications = applications, selectedTab = ApplicationFilterTab.WITHDRAWN),
                    onSelectTab = {},
                    onApplicationClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No applications here").assertIsDisplayed()
    }

    @Test
    fun clickingATabInvokesOnSelectTab() {
        var selected: ApplicationFilterTab? = null

        composeTestRule.setContent {
            LocalSkillTheme {
                ApplicationsContent(
                    uiState = ApplicationsUiState(applications = applications, selectedTab = ApplicationFilterTab.ALL),
                    onSelectTab = { selected = it },
                    onApplicationClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Hired").performClick()

        assert(selected == ApplicationFilterTab.HIRED)
    }

    @Test
    fun clickingAnApplicationRowInvokesOnApplicationClick() {
        var clickedId: String? = null

        composeTestRule.setContent {
            LocalSkillTheme {
                ApplicationsContent(
                    uiState = ApplicationsUiState(applications = applications, selectedTab = ApplicationFilterTab.ALL),
                    onSelectTab = {},
                    onApplicationClick = { clickedId = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Android Developer").performClick()

        assert(clickedId == "1")
    }
}
