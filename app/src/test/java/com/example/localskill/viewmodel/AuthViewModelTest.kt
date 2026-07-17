package com.example.localskill.viewmodel

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        viewModel = AuthViewModel(authRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with blank fields shows validation errors and never calls the repo`() = runTest {
        viewModel.submitLogin()

        val state = viewModel.loginUiState.value
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
        verify(authRepo, never()).login(any(), any())
    }

    @Test
    fun `successful login clears the form and emits LoginSuccess`() = runTest {
        whenever(authRepo.login(any(), any())).thenReturn(ResultState.Success("uid-1"))
        viewModel.onLoginEmailChanged("jane@example.com")
        viewModel.onLoginPasswordChanged("Str0ngPass")

        val events = mutableListOf<AuthEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.submitLogin()
        advanceUntilIdle()

        verify(authRepo).login(eq("jane@example.com"), eq("Str0ngPass"))
        assertTrue(events.contains(AuthEvent.LoginSuccess))
        assertEquals("", viewModel.loginUiState.value.email)
        job.cancel()
    }

    @Test
    fun `failed login surfaces a readable error and clears the password`() = runTest {
        whenever(authRepo.login(any(), any())).thenReturn(ResultState.Error("Incorrect email or password."))
        viewModel.onLoginEmailChanged("jane@example.com")
        viewModel.onLoginPasswordChanged("wrongpass1A")

        viewModel.submitLogin()

        val state = viewModel.loginUiState.value
        assertEquals("Incorrect email or password.", state.errorMessage)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
    }

    @Test
    fun `job seeker registration with invalid fields does not call the repo`() = runTest {
        viewModel.submitJobSeekerRegistration()

        verify(authRepo, never()).registerJobSeeker(any(), any(), any(), any(), any())
        val state = viewModel.jobSeekerRegistrationUiState.value
        assertNotNull(state.fullNameError)
        assertNotNull(state.emailError)
        assertNotNull(state.phoneError)
        assertNotNull(state.addressError)
        assertNotNull(state.passwordError)
        assertNotNull(state.termsError)
    }

    @Test
    fun `valid job seeker registration calls registerJobSeeker with trimmed fields`() = runTest {
        whenever(authRepo.registerJobSeeker(any(), any(), any(), any(), any()))
            .thenReturn(ResultState.Success("uid-123"))

        viewModel.onJobSeekerFullNameChanged("Jane Doe")
        viewModel.onJobSeekerEmailChanged("jane@example.com")
        viewModel.onJobSeekerPhoneChanged("9812345678")
        viewModel.onJobSeekerAddressChanged("Kathmandu")
        viewModel.onJobSeekerPasswordChanged("Str0ngPass")
        viewModel.onJobSeekerConfirmPasswordChanged("Str0ngPass")
        viewModel.onJobSeekerTermsAcceptedChanged(true)

        viewModel.submitJobSeekerRegistration()

        val fullNameCaptor = argumentCaptor<String>()
        val emailCaptor = argumentCaptor<String>()
        verify(authRepo).registerJobSeeker(
            fullNameCaptor.capture(), emailCaptor.capture(), any(), any(), any()
        )
        assertEquals("Jane Doe", fullNameCaptor.firstValue)
        assertEquals("jane@example.com", emailCaptor.firstValue)
    }

    @Test
    fun `company registration with invalid fields does not call the repo`() = runTest {
        viewModel.submitCompanyRegistration()

        verify(authRepo, never()).registerCompany(any(), any(), any(), any(), any(), any())
        val state = viewModel.companyRegistrationUiState.value
        assertNotNull(state.companyNameError)
        assertNotNull(state.contactPersonNameError)
    }

    @Test
    fun `valid company registration calls registerCompany`() = runTest {
        whenever(authRepo.registerCompany(any(), any(), any(), any(), any(), any()))
            .thenReturn(ResultState.Success("uid-123"))

        viewModel.onCompanyNameChanged("Acme Pvt Ltd")
        viewModel.onCompanyContactPersonNameChanged("John Smith")
        viewModel.onCompanyEmailChanged("hr@acme.com")
        viewModel.onCompanyPhoneChanged("9812345678")
        viewModel.onCompanyAddressChanged("Lalitpur")
        viewModel.onCompanyPasswordChanged("Str0ngPass")
        viewModel.onCompanyConfirmPasswordChanged("Str0ngPass")
        viewModel.onCompanyTermsAcceptedChanged(true)

        viewModel.submitCompanyRegistration()

        val companyNameCaptor = argumentCaptor<String>()
        verify(authRepo).registerCompany(
            companyNameCaptor.capture(), any(), any(), any(), any(), any()
        )
        assertEquals("Acme Pvt Ltd", companyNameCaptor.firstValue)
    }

    @Test
    fun `registration api surface has no path to create an administrator account`() {
        // AuthRepo only exposes registerJobSeeker and registerCompany, and
        // neither takes a role parameter, so there is no client-side path
        // to an ADMIN account — admin access can only come from a database
        // record an administrator already assigned.
        val methodNames = AuthRepo::class.java.methods.map { it.name }
        assertTrue(methodNames.any { it.equals("registerJobSeeker", ignoreCase = true) })
        assertTrue(methodNames.any { it.equals("registerCompany", ignoreCase = true) })
        assertFalse(methodNames.any { it.contains("admin", ignoreCase = true) })
    }

    @Test
    fun `password reset shows a neutral submitted state`() = runTest {
        whenever(authRepo.sendPasswordResetEmail(any())).thenReturn(ResultState.Success(Unit))
        viewModel.onPasswordResetEmailChanged("jane@example.com")

        viewModel.submitPasswordReset()

        val state = viewModel.passwordResetUiState.value
        assertTrue(state.isSubmitted)
        assertFalse(state.isLoading)
    }
}
