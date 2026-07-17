package com.example.localskill.viewmodel

import com.example.localskill.model.JobCategoryModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobSeekerProfileModel
import com.example.localskill.model.UserModel
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.JobRepo
import com.example.localskill.repo.JobSeekerProfileRepo
import com.example.localskill.repo.SavedJobRepo
import com.example.localskill.repo.UserRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class JobSeekerHomeViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var userRepo: UserRepo
    private lateinit var jobRepo: JobRepo
    private lateinit var savedJobRepo: SavedJobRepo
    private lateinit var profileRepo: JobSeekerProfileRepo
    private lateinit var viewModel: JobSeekerHomeViewModel

    private val savedIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    private val profile = JobSeekerProfileModel(userId = "uid-123", headline = "Developer")

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        whenever(authRepo.currentUserId()).thenReturn("uid-123")
        userRepo = mock()
        whenever(userRepo.getUserById("uid-123")).thenReturn(ResultState.Success(UserModel(id = "uid-123", fullName = "Jane Doe")))
        jobRepo = mock()
        whenever(jobRepo.getCategories()).thenReturn(ResultState.Success(listOf(JobCategoryModel(id = "cat-1", name = "Engineering"))))
        whenever(jobRepo.getFeaturedJobs(any())).thenReturn(
            ResultState.Success(listOf(JobModel(id = "1", title = "Android Developer", featured = true, createdAt = 200L)))
        )
        whenever(jobRepo.getRecentJobs(any())).thenReturn(
            ResultState.Success(
                listOf(
                    JobModel(id = "1", title = "Android Developer", featured = true, createdAt = 200L),
                    JobModel(id = "2", title = "Backend Developer", createdAt = 100L)
                )
            )
        )
        savedJobRepo = mock()
        whenever(savedJobRepo.observeSavedJobIds("uid-123")).thenReturn(savedIdsFlow)
        profileRepo = mock()
        whenever(profileRepo.getProfile("uid-123")).thenReturn(ResultState.Success(profile))
        viewModel = JobSeekerHomeViewModel(authRepo, userRepo, jobRepo, savedJobRepo, profileRepo)
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
        assertEquals(profile.completionPercentage, viewModel.uiState.value.profileCompletion)
    }

    @Test
    fun `toggleSaveJob updates saved state via the repo flow`() = runTest {
        whenever(savedJobRepo.saveJob("uid-123", "1")).thenAnswer {
            savedIdsFlow.value = savedIdsFlow.value + "1"
            ResultState.Success(Unit)
        }

        viewModel.toggleSaveJob("1")
        advanceUntilIdle()

        assertEquals(setOf("1"), viewModel.uiState.value.savedJobIds)
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
