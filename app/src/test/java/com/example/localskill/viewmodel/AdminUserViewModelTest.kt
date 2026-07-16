package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeAdminRepo
import com.example.localskill.fakes.FakeAuthRepo
import com.example.localskill.model.AccountStatus
import com.example.localskill.model.UserModel
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
class AdminUserViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakeAdminRepo: FakeAdminRepo
    private lateinit var viewModel: AdminUserViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = true }
        fakeAdminRepo = FakeAdminRepo().apply {
            users["uid-123"] = UserModel(id = "uid-123", fullName = "Admin", accountStatus = AccountStatus.ACTIVE.name)
            users["target-1"] = UserModel(id = "target-1", fullName = "Target User", accountStatus = AccountStatus.ACTIVE.name)
        }
        viewModel = AdminUserViewModel(fakeAuthRepo, fakeAdminRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `suspendUser suspends a different account`() = runTest {
        viewModel.suspendUser("target-1")

        assertEquals(AccountStatus.SUSPENDED.name, fakeAdminRepo.users.getValue("target-1").accountStatus)
    }

    @Test
    fun `an administrator cannot suspend their own account`() = runTest {
        viewModel.suspendUser("uid-123")

        assertEquals(AccountStatus.ACTIVE.name, fakeAdminRepo.users.getValue("uid-123").accountStatus)
    }

    @Test
    fun `reactivateUser restores a suspended account`() = runTest {
        fakeAdminRepo.users["target-1"] = fakeAdminRepo.users.getValue("target-1").copy(accountStatus = AccountStatus.SUSPENDED.name)

        viewModel.reactivateUser("target-1")

        assertEquals(AccountStatus.ACTIVE.name, fakeAdminRepo.users.getValue("target-1").accountStatus)
    }

    @Test
    fun `loadUsers exposes the current admin id for self-suspend UI guards`() = runTest {
        viewModel.loadUsers()

        assertEquals("uid-123", viewModel.uiState.value.currentAdminId)
        assertEquals(2, viewModel.uiState.value.users.size)
    }
}
