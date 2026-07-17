package com.example.localskill.viewmodel

import com.example.localskill.model.JobModel
import com.example.localskill.model.JobReportModel
import com.example.localskill.repo.ApplicationRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.JobRepo
import com.example.localskill.repo.ReportRepo
import com.example.localskill.repo.SavedJobRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class JobViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var jobRepo: JobRepo
    private lateinit var savedJobRepo: SavedJobRepo
    private lateinit var applicationRepo: ApplicationRepo
    private lateinit var reportRepo: ReportRepo
    private lateinit var viewModel: JobViewModel

    private val savedIdsFlow = MutableStateFlow<Set<String>>(emptySet())

    private val jobs = listOf(
        JobModel(id = "1", title = "Android Developer", location = "Kathmandu", jobType = "FULL_TIME"),
        JobModel(id = "2", title = "iOS Developer", location = "Pokhara", jobType = "INTERNSHIP")
    )

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        whenever(authRepo.currentUserId()).thenReturn("uid-123")
        jobRepo = mock()
        whenever(jobRepo.getCategories()).thenReturn(ResultState.Success(emptyList()))
        savedJobRepo = mock()
        whenever(savedJobRepo.observeSavedJobIds("uid-123")).thenReturn(savedIdsFlow)
        applicationRepo = mock()
        reportRepo = mock()
        viewModel = JobViewModel(authRepo, jobRepo, savedJobRepo, applicationRepo, reportRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ensureJobsLoaded loads active jobs once`() = runTest {
        whenever(jobRepo.getActiveJobs()).thenReturn(ResultState.Success(jobs))

        viewModel.ensureJobsLoaded()
        assertEquals(2, viewModel.searchUiState.value.results.size)

        // A second call must not trigger another load (guarded by jobsLoaded flag).
        viewModel.ensureJobsLoaded()
        assertEquals(2, viewModel.searchUiState.value.results.size)
        verify(jobRepo, times(1)).getActiveJobs()
    }

    @Test
    fun `initial query seeds the filter`() = runTest {
        whenever(jobRepo.getActiveJobs()).thenReturn(ResultState.Success(jobs))

        viewModel.ensureJobsLoaded(initialQuery = "android")
        assertEquals(1, viewModel.searchUiState.value.results.size)
        assertEquals("1", viewModel.searchUiState.value.results.first().id)
    }

    @Test
    fun `updateFilter narrows results immediately`() = runTest {
        whenever(jobRepo.getActiveJobs()).thenReturn(ResultState.Success(jobs))

        viewModel.ensureJobsLoaded()
        viewModel.updateFilter { it.copy(jobType = "INTERNSHIP") }
        assertEquals(listOf("2"), viewModel.searchUiState.value.results.map { it.id })
    }

    @Test
    fun `clearAllFilters keeps query but drops other filters`() = runTest {
        whenever(jobRepo.getActiveJobs()).thenReturn(ResultState.Success(jobs))

        viewModel.ensureJobsLoaded()
        viewModel.updateFilter { it.copy(query = "developer", jobType = "INTERNSHIP") }
        viewModel.clearAllFilters()

        val filter = viewModel.searchUiState.value.filter
        assertEquals("developer", filter.query)
        assertEquals(null, filter.jobType)
    }

    @Test
    fun `toggleSaveJob saves then unsaves`() = runTest {
        whenever(savedJobRepo.saveJob("uid-123", "1")).thenAnswer {
            savedIdsFlow.value = savedIdsFlow.value + "1"
            ResultState.Success(Unit)
        }
        whenever(savedJobRepo.unsaveJob("uid-123", "1")).thenAnswer {
            savedIdsFlow.value = savedIdsFlow.value - "1"
            ResultState.Success(Unit)
        }

        viewModel.toggleSaveJob("1")
        advanceUntilIdle()
        assertTrue(viewModel.searchUiState.value.savedJobIds.contains("1"))

        viewModel.toggleSaveJob("1")
        advanceUntilIdle()
        assertTrue(!viewModel.searchUiState.value.savedJobIds.contains("1"))
    }

    @Test
    fun `reportJob submits a report for the logged in user`() = runTest {
        whenever(jobRepo.getJobById("1")).thenReturn(ResultState.Success(jobs[0]))
        whenever(applicationRepo.hasApplied("uid-123", "1")).thenReturn(ResultState.Success(false))
        whenever(reportRepo.submitReport(any())).thenReturn(
            ResultState.Success(JobReportModel(id = "report-1"))
        )

        viewModel.loadJobDetails("1")
        viewModel.reportJob("1", "Spam or misleading", "Looks fake")

        val captor = argumentCaptor<JobReportModel>()
        verify(reportRepo).submitReport(captor.capture())
        assertEquals("uid-123", captor.firstValue.reporterId)
        assertEquals("1", captor.firstValue.targetId)
        assertEquals("Spam or misleading", captor.firstValue.reason)
    }

    @Test
    fun `reportJob rejects a duplicate report against the same job`() = runTest {
        whenever(jobRepo.getJobById("1")).thenReturn(ResultState.Success(jobs[0]))
        whenever(applicationRepo.hasApplied("uid-123", "1")).thenReturn(ResultState.Success(false))
        whenever(reportRepo.submitReport(any())).thenReturn(
            ResultState.Success(JobReportModel(id = "report-1")),
            ResultState.Error("You have already reported this.")
        )

        viewModel.loadJobDetails("1")
        viewModel.reportJob("1", "Spam or misleading", "")
        viewModel.reportJob("1", "Scam or fraudulent", "")

        verify(reportRepo, times(2)).submitReport(any())
    }
}
