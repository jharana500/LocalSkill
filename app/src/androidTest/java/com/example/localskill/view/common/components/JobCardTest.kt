package com.example.localskill.view.common.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.localskill.model.JobModel
import com.example.localskill.view.theme.LocalSkillTheme
import org.junit.Rule
import org.junit.Test

class JobCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val job = JobModel(
        id = "job-1",
        title = "Android Developer",
        companyName = "Acme Corp",
        location = "Kathmandu",
        jobType = "FULL_TIME"
    )

    @Test
    fun unsavedJobShowsSaveIconAndTogglesOnClick() {
        var toggled = false

        composeTestRule.setContent {
            LocalSkillTheme {
                JobCard(
                    job = job,
                    isSaved = false,
                    onSaveToggle = { toggled = true },
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Android Developer").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Save job").performClick()

        assert(toggled)
    }

    @Test
    fun savedJobShowsUnsaveIcon() {
        composeTestRule.setContent {
            LocalSkillTheme {
                JobCard(
                    job = job,
                    isSaved = true,
                    onSaveToggle = {},
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Unsave job").assertIsDisplayed()
    }

    @Test
    fun clickingCardInvokesOnClick() {
        var clicked = false

        composeTestRule.setContent {
            LocalSkillTheme {
                JobCard(
                    job = job,
                    isSaved = false,
                    onSaveToggle = {},
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Acme Corp").performClick()

        assert(clicked)
    }
}
