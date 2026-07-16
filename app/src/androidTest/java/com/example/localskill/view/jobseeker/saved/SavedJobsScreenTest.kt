package com.example.localskill.view.jobseeker.saved

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.localskill.model.JobModel
import com.example.localskill.view.theme.LocalSkillTheme
import com.example.localskill.viewmodel.SavedJobsUiState
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test

class SavedJobsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyStateIsShownWhenNoSavedJobs() {
        composeTestRule.setContent {
            LocalSkillTheme {
                SavedJobsContent(
                    uiState = SavedJobsUiState(),
                    events = emptyFlow(),
                    onJobClick = {},
                    onUnsaveJob = {},
                    onRetry = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No saved jobs yet").assertIsDisplayed()
    }

    @Test
    fun populatedStateListsSavedJobs() {
        composeTestRule.setContent {
            LocalSkillTheme {
                SavedJobsContent(
                    uiState = SavedJobsUiState(
                        savedJobs = listOf(
                            JobModel(id = "1", title = "Android Developer", companyName = "Acme")
                        )
                    ),
                    events = emptyFlow(),
                    onJobClick = {},
                    onUnsaveJob = {},
                    onRetry = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Android Developer").assertIsDisplayed()
    }

    @Test
    fun unsaveButtonInvokesCallbackWithJobId() {
        var unsavedId: String? = null

        composeTestRule.setContent {
            LocalSkillTheme {
                SavedJobsContent(
                    uiState = SavedJobsUiState(
                        savedJobs = listOf(
                            JobModel(id = "1", title = "Android Developer", companyName = "Acme")
                        )
                    ),
                    events = emptyFlow(),
                    onJobClick = {},
                    onUnsaveJob = { unsavedId = it },
                    onRetry = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Unsave job").performClick()

        assert(unsavedId == "1")
    }

    @Test
    fun retryButtonIsShownOnErrorWithNoSavedJobs() {
        var retried = false

        composeTestRule.setContent {
            LocalSkillTheme {
                SavedJobsContent(
                    uiState = SavedJobsUiState(errorMessage = "Network error"),
                    events = emptyFlow(),
                    onJobClick = {},
                    onUnsaveJob = {},
                    onRetry = { retried = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Retry").performClick()

        assert(retried)
    }
}
