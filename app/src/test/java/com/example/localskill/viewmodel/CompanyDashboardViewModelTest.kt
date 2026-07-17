package com.example.localskill.viewmodel

import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.model.CompanyDashboardStatsModel
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobStatus
import com.example.localskill.repo.ApplicantRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.CompanyJobRepo
import com.example.localskill.repo.CompanyRepo
import com.example.localskill.utils.ResultState
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CompanyDashboardViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var companyRepo: CompanyRepo
    private lateinit var companyJobRepo: CompanyJobRepo
    private lateinit var applicantRepo: ApplicantRepo
    private lateinit var viewModel: CompanyDashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        whenever(authRepo.currentUserId()).thenReturn("uid-123")
        companyRepo = mock()
        companyJobRepo = mock()
        applicantRepo = mock()
        viewModel = CompanyDashboardViewModel(authRepo, companyRepo, companyJobRepo, applicantRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDashboard aggregates real job and application counts`() = runTest {
        whenever(companyRepo.getCompany("uid-123"))
            .thenReturn(ResultState.Success(CompanyModel(id = "uid-123", companyName = "Acme")))
        whenever(companyJobRepo.getCompanyJobs("uid-123")).thenReturn(
            ResultState.Success(
                listOf(
                    JobModel(id = "job-1", companyId = "uid-123", status = JobStatus.ACTIVE.name),
                    JobModel(id = "job-2", companyId = "uid-123", status = JobStatus.DRAFT.name),
                    JobModel(id = "job-3", companyId = "uid-123", status = JobStatus.CLOSED.name)
                )
            )
        )
        whenever(applicantRepo.getApplicantStats("uid-123")).thenReturn(
            ResultState.Success(CompanyDashboardStatsModel(totalApplications = 2, hired = 1))
        )
        whenever(applicantRepo.getCompanyApplications("uid-123")).thenReturn(
            ResultState.Success(
                listOf(
                    ApplicationModel(id = "app-1", companyId = "uid-123", status = ApplicationStatus.APPLIED.name),
                    ApplicationModel(id = "app-2", companyId = "uid-123", status = ApplicationStatus.HIRED.name)
                )
            )
        )

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
