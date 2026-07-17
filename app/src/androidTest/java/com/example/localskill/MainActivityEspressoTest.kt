package com.example.localskill

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun waitForActivityToSettle() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun checkContentRootWithRetries(assertion: () -> Unit) {
        var lastError: Throwable? = null
        repeat(3) { attempt ->
            try {
                assertion()
                return
            } catch (error: Throwable) {
                lastError = error
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                Thread.sleep(1000L * (attempt + 1))
            }
        }
        throw lastError!!
    }

    @Test
    fun activityLaunchesAndContentRootIsDisplayed() {
        checkContentRootWithRetries {
            onView(withId(android.R.id.content)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun contentRootRendersAtLeastOneChildView() {
        checkContentRootWithRetries {
            onView(withId(android.R.id.content)).check(matches(hasMinimumChildCount(1)))
        }
    }
}
