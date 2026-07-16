package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeAuthRepo
import com.example.localskill.fakes.FakeJobRepo
import com.example.localskill.fakes.FakeApplicationRepo
import com.example.localskill.fakes.FakeReportRepo
import com.example.localskill.fakes.FakeSavedJobRepo
import com.example.localskill.model.JobModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JobViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakeJobRepo: FakeJobRepo
    private lateinit var fakeSavedJobRepo: FakeSavedJobRepo
    private lateinit var fakeReportRepo: FakeReportRepo
    private lateinit var viewModel: JobViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = true }
        fakeJobRepo = FakeJobRepo().apply {
            activeJobs = listOf(
                JobModel(id = "1", title = "Android Developer", location = "Kathmandu", jobType = "FULL_TIME"),
                JobModel(id = "2", title = "iOS Developer", location = "Pokhara", jobType = "INTERNSHIP")
            )
        }
        fakeSavedJobRepo = FakeSavedJobRepo()
        fakeReportRepo = FakeReportRepo()
        viewModel = JobViewModel(fakeAuthRepo, fakeJobRepo, fakeSavedJobRepo, FakeApplicationRepo(), fakeReportRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ensureJobsLoaded loads active jobs once`() = runTest {
        viewModel.ensureJobsLoaded()
        assertEquals(2, viewModel.searchUiState.value.results.size)

        // A second call must not trigger another load (guarded by jobsLoaded flag).
        fakeJobRepo.activeJobs = emptyList()
        viewModel.ensureJobsLoaded()
        assertEquals(2, viewModel.searchUiState.value.results.size)
    }

    @Test
    fun `initial query seeds the filter`() = runTest {
        viewModel.ensureJobsLoaded(initialQuery = "android")
        assertEquals(1, viewModel.searchUiState.value.results.size)
        assertEquals("1", viewModel.searchUiState.value.results.first().id)
    }

    @Test
    fun `updateFilter narrows results immediately`() = runTest {
        viewModel.ensureJobsLoaded()
        viewModel.updateFilter { it.copy(jobType = "INTERNSHIP") }
        assertEquals(listOf("2"), viewModel.searchUiState.value.results.map { it.id })
    }

    @Test
    fun `clearAllFilters keeps query but drops other filters`() = runTest {
        viewModel.ensureJobsLoaded()
        viewModel.updateFilter { it.copy(query = "developer", jobType = "INTERNSHIP") }
        viewModel.clearAllFilters()

        val filter = viewModel.searchUiState.value.filter
        assertEquals("developer", filter.query)
        assertEquals(null, filter.jobType)
    }

    @Test
    fun `toggleSaveJob saves then unsaves`() = runTest {
        viewModel.toggleSaveJob("1")
        assertTrue(fakeSavedJobRepo.savedIdsByUser["uid-123"]?.contains("1") == true)

        viewModel.toggleSaveJob("1")
        assertTrue(fakeSavedJobRepo.savedIdsByUser["uid-123"]?.contains("1") != true)
    }

    @Test
    fun `reportJob submits a report for the logged in user`() = runTest {
        viewModel.loadJobDetails("1")
        viewModel.reportJob("1", "Spam or misleading", "Looks fake")

        val submitted = fakeReportRepo.reports.values.single()
        assertEquals("uid-123", submitted.reporterId)
        assertEquals("1", submitted.targetId)
        assertEquals("Spam or misleading", submitted.reason)
    }

    @Test
    fun `reportJob rejects a duplicate report against the same job`() = runTest {
        viewModel.loadJobDetails("1")
        viewModel.reportJob("1", "Spam or misleading", "")
        viewModel.reportJob("1", "Scam or fraudulent", "")

        assertEquals(1, fakeReportRepo.reports.size)
    }
}
