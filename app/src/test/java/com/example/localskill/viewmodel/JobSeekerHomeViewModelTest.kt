package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeApplicationRepo
import com.example.localskill.fakes.FakeAuthRepo
import com.example.localskill.fakes.FakeJobRepo
import com.example.localskill.fakes.FakeJobSeekerProfileRepo
import com.example.localskill.fakes.FakeSavedJobRepo
import com.example.localskill.fakes.FakeUserRepo
import com.example.localskill.model.JobCategoryModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobSeekerProfileModel
import com.example.localskill.model.UserModel
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JobSeekerHomeViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakeUserRepo: FakeUserRepo
    private lateinit var fakeJobRepo: FakeJobRepo
    private lateinit var fakeSavedJobRepo: FakeSavedJobRepo
    private lateinit var fakeProfileRepo: FakeJobSeekerProfileRepo
    private lateinit var viewModel: JobSeekerHomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = true }
        fakeUserRepo = FakeUserRepo().apply {
            userResult = ResultState.Success(UserModel(id = "uid-123", fullName = "Jane Doe"))
        }
        fakeJobRepo = FakeJobRepo().apply {
            activeJobs = listOf(
                JobModel(id = "1", title = "Android Developer", featured = true, createdAt = 200L),
                JobModel(id = "2", title = "Backend Developer", createdAt = 100L)
            )
            categories = listOf(JobCategoryModel(id = "cat-1", name = "Engineering"))
        }
        fakeSavedJobRepo = FakeSavedJobRepo()
        fakeProfileRepo = FakeJobSeekerProfileRepo().apply {
            profiles["uid-123"] = JobSeekerProfileModel(userId = "uid-123", headline = "Developer")
        }
        viewModel = JobSeekerHomeViewModel(fakeAuthRepo, fakeUserRepo, fakeJobRepo, fakeSavedJobRepo, fakeProfileRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDashboard populates name categories and jobs`() = runTest {
        viewModel.loadDashboard()

        val state = viewModel.uiState.value
        assertEquals("Jane Doe", state.fullName)
        assertEquals(1, state.categories.size)
        assertEquals(listOf("1"), state.featuredJobs.map { it.id })
        assertEquals(listOf("1", "2"), state.recentJobs.map { it.id })
    }

    @Test
    fun `loadDashboard computes profile completion from the loaded profile`() = runTest {
        viewModel.loadDashboard()
        assertEquals(
            fakeProfileRepo.profiles["uid-123"]!!.completionPercentage,
            viewModel.uiState.value.profileCompletion
        )
    }

    @Test
    fun `toggleSaveJob updates saved state via the repo flow`() = runTest {
        viewModel.toggleSaveJob("1")
        assertEquals(setOf("1"), fakeSavedJobRepo.savedIdsByUser["uid-123"])
    }

    @Test
    fun `onSearchSubmit emits a navigate event with the trimmed query`() = runTest {
        val events = mutableListOf<JobSeekerHomeEvent>()
        val collectJob = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onSearchQueryChanged("  android  ")
        viewModel.onSearchSubmit()
        advanceUntilIdle()

        assertTrue(events.contains(JobSeekerHomeEvent.NavigateToExplore("android")))
        collectJob.cancel()
    }
}
