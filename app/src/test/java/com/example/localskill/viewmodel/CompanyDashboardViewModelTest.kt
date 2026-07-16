package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeApplicantRepo
import com.example.localskill.fakes.FakeAuthRepo
import com.example.localskill.fakes.FakeCompanyJobRepo
import com.example.localskill.fakes.FakeCompanyRepo
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompanyDashboardViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakeCompanyRepo: FakeCompanyRepo
    private lateinit var fakeCompanyJobRepo: FakeCompanyJobRepo
    private lateinit var fakeApplicantRepo: FakeApplicantRepo
    private lateinit var viewModel: CompanyDashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = true }
        fakeCompanyRepo = FakeCompanyRepo().apply {
            companies["uid-123"] = CompanyModel(id = "uid-123", companyName = "Acme")
        }
        fakeCompanyJobRepo = FakeCompanyJobRepo(fakeCompanyRepo).apply {
            jobs["job-1"] = JobModel(id = "job-1", companyId = "uid-123", status = JobStatus.ACTIVE.name)
            jobs["job-2"] = JobModel(id = "job-2", companyId = "uid-123", status = JobStatus.DRAFT.name)
            jobs["job-3"] = JobModel(id = "job-3", companyId = "uid-123", status = JobStatus.CLOSED.name)
        }
        fakeApplicantRepo = FakeApplicantRepo().apply {
            applications["app-1"] = ApplicationModel(id = "app-1", companyId = "uid-123", status = ApplicationStatus.APPLIED.name)
            applications["app-2"] = ApplicationModel(id = "app-2", companyId = "uid-123", status = ApplicationStatus.HIRED.name)
        }
        viewModel = CompanyDashboardViewModel(fakeAuthRepo, fakeCompanyRepo, fakeCompanyJobRepo, fakeApplicantRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDashboard aggregates real job and application counts`() = runTest {
        viewModel.loadDashboard()

        val stats = viewModel.uiState.value.stats
        assertEquals(1, stats.activeJobs)
        assertEquals(1, stats.draftJobs)
        assertEquals(1, stats.closedJobs)
        assertEquals(2, stats.totalApplications)
        assertEquals(1, stats.hired)
        assertEquals("Acme", viewModel.uiState.value.company.companyName)
    }
}
