package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeAuthRepo
import com.example.localskill.fakes.FakeReportRepo
import com.example.localskill.model.JobReportModel
import com.example.localskill.model.ReportStatus
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
class AdminReportViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakeReportRepo: FakeReportRepo
    private lateinit var viewModel: AdminReportViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = true }
        fakeReportRepo = FakeReportRepo().apply {
            reports["report-1"] = JobReportModel(id = "report-1", reporterId = "job-seeker-1", targetId = "job-1", reason = "Spam")
        }
        viewModel = AdminReportViewModel(fakeAuthRepo, fakeReportRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `resolveReport marks the report RESOLVED with a note`() = runTest {
        viewModel.loadReportDetails("report-1")
        viewModel.resolveReport("Job removed.")

        val report = fakeReportRepo.reports.getValue("report-1")
        assertEquals(ReportStatus.RESOLVED.name, report.status)
        assertEquals("Job removed.", report.resolutionNote)
        assertEquals("uid-123", report.resolvedBy)
    }

    @Test
    fun `rejectReport marks the report REJECTED`() = runTest {
        viewModel.loadReportDetails("report-1")
        viewModel.rejectReport("No violation found.")

        assertEquals(ReportStatus.REJECTED.name, fakeReportRepo.reports.getValue("report-1").status)
    }
}
