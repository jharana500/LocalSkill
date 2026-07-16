package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeAppPreferencesRepo
import com.example.localskill.fakes.FakeAuthRepo
import com.example.localskill.model.AccountStatus
import com.example.localskill.model.AuthSessionModel
import com.example.localskill.model.UserRole
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppSessionViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakePreferencesRepo: FakeAppPreferencesRepo

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sessionFor(role: UserRole, accountStatus: AccountStatus, verified: Boolean = true) =
        AuthSessionModel(
            userId = "uid-1",
            email = "user@example.com",
            isEmailVerified = verified,
            role = role.name,
            accountStatus = accountStatus.name
        )

    @Test
    fun `routes to onboarding when not yet completed`() = runTest {
        fakeAuthRepo = FakeAuthRepo()
        fakePreferencesRepo = FakeAppPreferencesRepo(initialOnboardingCompleted = false)

        val viewModel = AppSessionViewModel(fakeAuthRepo, fakePreferencesRepo)

        assertEquals(SessionDestination.ONBOARDING, viewModel.uiState.value.destination)
    }

    @Test
    fun `routes to role selection when no user is logged in`() = runTest {
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = false }
        fakePreferencesRepo = FakeAppPreferencesRepo(initialOnboardingCompleted = true)

        val viewModel = AppSessionViewModel(fakeAuthRepo, fakePreferencesRepo)

        assertEquals(SessionDestination.ROLE_SELECTION, viewModel.uiState.value.destination)
    }

    @Test
    fun `routes to email verification when logged in but unverified`() = runTest {
        fakeAuthRepo = FakeAuthRepo().apply {
            loggedIn = true
            restoreSessionResult =
                ResultState.Success(sessionFor(UserRole.JOB_SEEKER, AccountStatus.ACTIVE, verified = false))
        }
        fakePreferencesRepo = FakeAppPreferencesRepo(initialOnboardingCompleted = true)

        val viewModel = AppSessionViewModel(fakeAuthRepo, fakePreferencesRepo)

        assertEquals(SessionDestination.EMAIL_VERIFICATION, viewModel.uiState.value.destination)
    }

    @Test
    fun `routes pending company to the restricted company graph`() = runTest {
        fakeAuthRepo = FakeAuthRepo().apply {
            loggedIn = true
            restoreSessionResult = ResultState.Success(sessionFor(UserRole.COMPANY, AccountStatus.PENDING))
        }
        fakePreferencesRepo = FakeAppPreferencesRepo(initialOnboardingCompleted = true)

        val viewModel = AppSessionViewModel(fakeAuthRepo, fakePreferencesRepo)

        assertEquals(SessionDestination.COMPANY_ENTRY, viewModel.uiState.value.destination)
        assertTrue(viewModel.uiState.value.companyRestrictedMode)
    }

    @Test
    fun `a verified active company is not in restricted mode`() = runTest {
        fakeAuthRepo = FakeAuthRepo().apply {
            loggedIn = true
            restoreSessionResult = ResultState.Success(sessionFor(UserRole.COMPANY, AccountStatus.ACTIVE))
        }
        fakePreferencesRepo = FakeAppPreferencesRepo(initialOnboardingCompleted = true)

        val viewModel = AppSessionViewModel(fakeAuthRepo, fakePreferencesRepo)

        assertEquals(SessionDestination.COMPANY_ENTRY, viewModel.uiState.value.destination)
        assertFalse(viewModel.uiState.value.companyRestrictedMode)
    }

    @Test
    fun `a suspended company is always blocked regardless of verification state`() = runTest {
        fakeAuthRepo = FakeAuthRepo().apply {
            loggedIn = true
            restoreSessionResult = ResultState.Success(sessionFor(UserRole.COMPANY, AccountStatus.SUSPENDED))
        }
        fakePreferencesRepo = FakeAppPreferencesRepo(initialOnboardingCompleted = true)

        val viewModel = AppSessionViewModel(fakeAuthRepo, fakePreferencesRepo)

        assertEquals(SessionDestination.ACCOUNT_STATUS, viewModel.uiState.value.destination)
    }

    @Test
    fun `routes active job seeker to job seeker entry`() = runTest {
        fakeAuthRepo = FakeAuthRepo().apply {
            loggedIn = true
            restoreSessionResult = ResultState.Success(sessionFor(UserRole.JOB_SEEKER, AccountStatus.ACTIVE))
        }
        fakePreferencesRepo = FakeAppPreferencesRepo(initialOnboardingCompleted = true)

        val viewModel = AppSessionViewModel(fakeAuthRepo, fakePreferencesRepo)

        assertEquals(SessionDestination.JOB_SEEKER_ENTRY, viewModel.uiState.value.destination)
    }

    @Test
    fun `routes active admin to admin entry`() = runTest {
        fakeAuthRepo = FakeAuthRepo().apply {
            loggedIn = true
            restoreSessionResult = ResultState.Success(sessionFor(UserRole.ADMIN, AccountStatus.ACTIVE))
        }
        fakePreferencesRepo = FakeAppPreferencesRepo(initialOnboardingCompleted = true)

        val viewModel = AppSessionViewModel(fakeAuthRepo, fakePreferencesRepo)

        assertEquals(SessionDestination.ADMIN_ENTRY, viewModel.uiState.value.destination)
    }

    @Test
    fun `logout clears the session and routes back to role selection`() = runTest {
        fakeAuthRepo = FakeAuthRepo().apply {
            loggedIn = true
            restoreSessionResult = ResultState.Success(sessionFor(UserRole.JOB_SEEKER, AccountStatus.ACTIVE))
        }
        fakePreferencesRepo = FakeAppPreferencesRepo(initialOnboardingCompleted = true)

        val viewModel = AppSessionViewModel(fakeAuthRepo, fakePreferencesRepo)
        assertEquals(SessionDestination.JOB_SEEKER_ENTRY, viewModel.uiState.value.destination)

        viewModel.logout()

        assertEquals(SessionDestination.ROLE_SELECTION, viewModel.uiState.value.destination)
        assertTrue(fakeAuthRepo.logoutCalled)
    }
}
