package com.example.localskill.viewmodel

import com.example.localskill.model.AccountStatus
import com.example.localskill.model.UserModel
import com.example.localskill.repo.AdminRepo
import com.example.localskill.repo.AuthRepo
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
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AdminUserViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var adminRepo: AdminRepo
    private lateinit var viewModel: AdminUserViewModel

    private val adminUser = UserModel(id = "uid-123", fullName = "Admin", accountStatus = AccountStatus.ACTIVE.name)
    private val targetUser = UserModel(id = "target-1", fullName = "Target User", accountStatus = AccountStatus.ACTIVE.name)

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        whenever(authRepo.currentUserId()).thenReturn("uid-123")
        adminRepo = mock()
        whenever(adminRepo.getAllUsers()).thenReturn(ResultState.Success(listOf(adminUser, targetUser)))
        viewModel = AdminUserViewModel(authRepo, adminRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `suspendUser suspends a different account`() = runTest {
        val suspendedTarget = targetUser.copy(accountStatus = AccountStatus.SUSPENDED.name)
        whenever(adminRepo.suspendUser("uid-123", "target-1")).thenReturn(ResultState.Success(Unit))
        whenever(adminRepo.getAllUsers()).thenReturn(ResultState.Success(listOf(adminUser, suspendedTarget)))

        viewModel.suspendUser("target-1")

        val user = viewModel.uiState.value.users.single { it.id == "target-1" }
        assertEquals(AccountStatus.SUSPENDED.name, user.accountStatus)
    }

    @Test
    fun `an administrator cannot suspend their own account`() = runTest {
        whenever(adminRepo.suspendUser("uid-123", "uid-123"))
            .thenReturn(ResultState.Error("You cannot suspend your own account."))

        val events = mutableListOf<AdminUserEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.suspendUser("uid-123")
        advanceUntilIdle()

        assertEquals(
            listOf(AdminUserEvent.ShowMessage("You cannot suspend your own account.")),
            events
        )
        assertEquals(0, viewModel.uiState.value.users.size)
        job.cancel()
    }

    @Test
    fun `reactivateUser restores a suspended account`() = runTest {
        val reactivatedTarget = targetUser.copy(accountStatus = AccountStatus.ACTIVE.name)
        whenever(adminRepo.reactivateUser("uid-123", "target-1")).thenReturn(ResultState.Success(Unit))
        whenever(adminRepo.getAllUsers()).thenReturn(ResultState.Success(listOf(adminUser, reactivatedTarget)))

        viewModel.reactivateUser("target-1")

        val user = viewModel.uiState.value.users.single { it.id == "target-1" }
        assertEquals(AccountStatus.ACTIVE.name, user.accountStatus)
    }

    @Test
    fun `loadUsers exposes the current admin id for self-suspend UI guards`() = runTest {
        viewModel.loadUsers()

        assertEquals("uid-123", viewModel.uiState.value.currentAdminId)
        assertEquals(2, viewModel.uiState.value.users.size)
    }
}
