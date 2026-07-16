package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeAdminRepo
import com.example.localskill.fakes.FakeReportRepo
import com.example.localskill.model.AdminActivityModel
import com.example.localskill.model.AdminActivityType
import com.example.localskill.model.AdminDashboardStatsModel
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
class AdminDashboardViewModelTest {

    private lateinit var fakeAdminRepo: FakeAdminRepo
    private lateinit var viewModel: AdminDashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAdminRepo = FakeAdminRepo().apply {
            dashboardStats = AdminDashboardStatsModel(totalUsers = 10, pendingCompanies = 3, openReports = 2)
            repeat(15) {
                activityLog.add(
                    AdminActivityModel(id = "activity-$it", actionType = AdminActivityType.COMPANY_APPROVED.name, createdAt = it.toLong())
                )
            }
        }
        viewModel = AdminDashboardViewModel(fakeAdminRepo, FakeReportRepo())
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
