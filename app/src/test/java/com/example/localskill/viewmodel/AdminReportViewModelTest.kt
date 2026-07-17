package com.example.localskill.viewmodel

import com.example.localskill.model.JobReportModel
import com.example.localskill.model.ReportStatus
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.ReportRepo
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
class AdminReportViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var reportRepo: ReportRepo
    private lateinit var viewModel: AdminReportViewModel

    private val pendingReport = JobReportModel(id = "report-1", reporterId = "job-seeker-1", targetId = "job-1", reason = "Spam")

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        whenever(authRepo.currentUserId()).thenReturn("uid-123")
        reportRepo = mock()
        whenever(reportRepo.getReportById("report-1")).thenReturn(ResultState.Success(pendingReport))
        viewModel = AdminReportViewModel(authRepo, reportRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `resolveReport marks the report RESOLVED with a note`() = runTest {
        val resolved = pendingReport.copy(status = ReportStatus.RESOLVED.name, resolutionNote = "Job removed.", resolvedBy = "uid-123")
        whenever(reportRepo.resolveReport("uid-123", "report-1", "Job removed.")).thenReturn(ResultState.Success(Unit))
        whenever(reportRepo.getReportById("report-1")).thenReturn(
            ResultState.Success(pendingReport),
            ResultState.Success(resolved)
        )

        viewModel.loadReportDetails("report-1")
        viewModel.resolveReport("Job removed.")

        val report = viewModel.detailsUiState.value.report
        assertEquals(ReportStatus.RESOLVED.name, report?.status)
        assertEquals("Job removed.", report?.resolutionNote)
        assertEquals("uid-123", report?.resolvedBy)
    }

    @Test
    fun `rejectReport marks the report REJECTED`() = runTest {
        val rejected = pendingReport.copy(status = ReportStatus.REJECTED.name)
        whenever(reportRepo.rejectReport("uid-123", "report-1", "No violation found.")).thenReturn(ResultState.Success(Unit))
        whenever(reportRepo.getReportById("report-1")).thenReturn(
            ResultState.Success(pendingReport),
            ResultState.Success(rejected)
        )

        viewModel.loadReportDetails("report-1")
        viewModel.rejectReport("No violation found.")

        assertEquals(ReportStatus.REJECTED.name, viewModel.detailsUiState.value.report?.status)
    }
}
