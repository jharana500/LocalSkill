package com.example.localskill.viewmodel

import com.example.localskill.model.AdminActivityModel
import com.example.localskill.model.AdminActivityType
import com.example.localskill.model.AdminDashboardStatsModel
import com.example.localskill.model.ReportStatus
import com.example.localskill.repo.AdminRepo
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
class AdminDashboardViewModelTest {

    private lateinit var adminRepo: AdminRepo
    private lateinit var reportRepo: ReportRepo
    private lateinit var viewModel: AdminDashboardViewModel

    private val activityLog = (0 until 15).map {
        AdminActivityModel(id = "activity-$it", actionType = AdminActivityType.COMPANY_APPROVED.name, createdAt = it.toLong())
    }

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        adminRepo = mock()
        reportRepo = mock()
        whenever(adminRepo.getDashboardStats()).thenReturn(
            ResultState.Success(AdminDashboardStatsModel(totalUsers = 10, pendingCompanies = 3, openReports = 2))
        )
        whenever(adminRepo.getPendingCompanies()).thenReturn(ResultState.Success(emptyList()))
        whenever(adminRepo.getAllUsers()).thenReturn(ResultState.Success(emptyList()))
        whenever(adminRepo.getAllJobsForModeration()).thenReturn(ResultState.Success(emptyList()))
        whenever(reportRepo.getReports(ReportStatus.PENDING.name)).thenReturn(ResultState.Success(emptyList()))
        whenever(adminRepo.getActivityLog(10)).thenReturn(ResultState.Success(activityLog.take(10)))
        whenever(adminRepo.getActivityLog(200)).thenReturn(ResultState.Success(activityLog))
        viewModel = AdminDashboardViewModel(adminRepo, reportRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDashboard exposes real repository stats`() = runTest {
        viewModel.loadDashboard()

        val state = viewModel.uiState.value
        assertEquals(10, state.stats.totalUsers)
        assertEquals(3, state.stats.pendingCompanies)
        assertEquals(2, state.stats.openReports)
    }

    @Test
    fun `loadActivityLog loads beyond the dashboard preview limit`() = runTest {
        viewModel.loadActivityLog()

        assertEquals(15, viewModel.uiState.value.recentActivity.size)
    }
}
