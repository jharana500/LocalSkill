package com.example.localskill.viewmodel

import com.example.localskill.model.AccountStatus
import com.example.localskill.model.AuthSessionModel
import com.example.localskill.model.UserRole
import com.example.localskill.repo.AppPreferencesRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.utils.ResultState
import com.example.localskill.view.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AppSessionViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var preferencesRepo: AppPreferencesRepo

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        preferencesRepo = mock()
        whenever(preferencesRepo.appTheme).thenReturn(flowOf(AppTheme.SYSTEM))
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

    private fun stubOnboardingCompleted(completed: Boolean) {
        whenever(preferencesRepo.isOnboardingCompleted).thenReturn(flowOf(completed))
    }

    @Test
    fun `routes to onboarding when not yet completed`() = runTest {
        stubOnboardingCompleted(false)

        val viewModel = AppSessionViewModel(authRepo, preferencesRepo)

        assertEquals(SessionDestination.ONBOARDING, viewModel.uiState.value.destination)
    }

    @Test
    fun `routes to role selection when no user is logged in`() = runTest {
        stubOnboardingCompleted(true)
        whenever(authRepo.isUserLoggedIn()).thenReturn(false)

        val viewModel = AppSessionViewModel(authRepo, preferencesRepo)

        assertEquals(SessionDestination.ROLE_SELECTION, viewModel.uiState.value.destination)
    }

    @Test
    fun `routes to email verification when logged in but unverified`() = runTest {
        stubOnboardingCompleted(true)
        whenever(authRepo.isUserLoggedIn()).thenReturn(true)
        whenever(authRepo.restoreSession()).thenReturn(
            ResultState.Success(sessionFor(UserRole.JOB_SEEKER, AccountStatus.ACTIVE, verified = false))
        )

        val viewModel = AppSessionViewModel(authRepo, preferencesRepo)

        assertEquals(SessionDestination.EMAIL_VERIFICATION, viewModel.uiState.value.destination)
    }

    @Test
    fun `routes pending company to the restricted company graph`() = runTest {
        stubOnboardingCompleted(true)
        whenever(authRepo.isUserLoggedIn()).thenReturn(true)
        whenever(authRepo.restoreSession()).thenReturn(
            ResultState.Success(sessionFor(UserRole.COMPANY, AccountStatus.PENDING))
        )

        val viewModel = AppSessionViewModel(authRepo, preferencesRepo)

        assertEquals(SessionDestination.COMPANY_ENTRY, viewModel.uiState.value.destination)
        assertTrue(viewModel.uiState.value.companyRestrictedMode)
    }

    @Test
    fun `a verified active company is not in restricted mode`() = runTest {
        stubOnboardingCompleted(true)
        whenever(authRepo.isUserLoggedIn()).thenReturn(true)
        whenever(authRepo.restoreSession()).thenReturn(
            ResultState.Success(sessionFor(UserRole.COMPANY, AccountStatus.ACTIVE))
        )

        val viewModel = AppSessionViewModel(authRepo, preferencesRepo)

        assertEquals(SessionDestination.COMPANY_ENTRY, viewModel.uiState.value.destination)
        assertFalse(viewModel.uiState.value.companyRestrictedMode)
    }

    @Test
    fun `a suspended company is always blocked regardless of verification state`() = runTest {
        stubOnboardingCompleted(true)
        whenever(authRepo.isUserLoggedIn()).thenReturn(true)
        whenever(authRepo.restoreSession()).thenReturn(
            ResultState.Success(sessionFor(UserRole.COMPANY, AccountStatus.SUSPENDED))
        )

        val viewModel = AppSessionViewModel(authRepo, preferencesRepo)

        assertEquals(SessionDestination.ACCOUNT_STATUS, viewModel.uiState.value.destination)
    }

    @Test
    fun `routes active job seeker to job seeker entry`() = runTest {
        stubOnboardingCompleted(true)
        whenever(authRepo.isUserLoggedIn()).thenReturn(true)
        whenever(authRepo.restoreSession()).thenReturn(
            ResultState.Success(sessionFor(UserRole.JOB_SEEKER, AccountStatus.ACTIVE))
        )

        val viewModel = AppSessionViewModel(authRepo, preferencesRepo)

        assertEquals(SessionDestination.JOB_SEEKER_ENTRY, viewModel.uiState.value.destination)
    }

    @Test
    fun `routes active admin to admin entry`() = runTest {
        stubOnboardingCompleted(true)
        whenever(authRepo.isUserLoggedIn()).thenReturn(true)
        whenever(authRepo.restoreSession()).thenReturn(
            ResultState.Success(sessionFor(UserRole.ADMIN, AccountStatus.ACTIVE))
        )

        val viewModel = AppSessionViewModel(authRepo, preferencesRepo)

        assertEquals(SessionDestination.ADMIN_ENTRY, viewModel.uiState.value.destination)
    }

    @Test
    fun `logout clears the session and routes back to role selection`() = runTest {
        stubOnboardingCompleted(true)
        whenever(authRepo.isUserLoggedIn()).thenReturn(true)
        whenever(authRepo.restoreSession()).thenReturn(
            ResultState.Success(sessionFor(UserRole.JOB_SEEKER, AccountStatus.ACTIVE))
        )

        val viewModel = AppSessionViewModel(authRepo, preferencesRepo)
        assertEquals(SessionDestination.JOB_SEEKER_ENTRY, viewModel.uiState.value.destination)

        viewModel.logout()

        assertEquals(SessionDestination.ROLE_SELECTION, viewModel.uiState.value.destination)
        verify(authRepo).logout()
    }
}
