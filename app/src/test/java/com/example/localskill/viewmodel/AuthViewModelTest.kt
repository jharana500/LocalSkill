package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeAuthRepo
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

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthRepo = FakeAuthRepo()
        viewModel = AuthViewModel(fakeAuthRepo)
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
        assertNull(fakeAuthRepo.lastLoginEmail)
    }

    @Test
    fun `successful login clears the form and emits LoginSuccess`() = runTest {
        fakeAuthRepo.loginResult = ResultState.Success("uid-1")
        viewModel.onLoginEmailChanged("jane@example.com")
        viewModel.onLoginPasswordChanged("Str0ngPass")

        val events = mutableListOf<AuthEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.submitLogin()
        advanceUntilIdle()

        assertEquals("jane@example.com", fakeAuthRepo.lastLoginEmail)
        assertTrue(events.contains(AuthEvent.LoginSuccess))
        assertEquals("", viewModel.loginUiState.value.email)
        job.cancel()
    }

    @Test
    fun `failed login surfaces a readable error and clears the password`() = runTest {
        fakeAuthRepo.loginResult = ResultState.Error("Incorrect email or password.")
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

        assertNull(fakeAuthRepo.lastJobSeekerRegistration)
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
        viewModel.onJobSeekerFullNameChanged("Jane Doe")
        viewModel.onJobSeekerEmailChanged("jane@example.com")
        viewModel.onJobSeekerPhoneChanged("9812345678")
        viewModel.onJobSeekerAddressChanged("Kathmandu")
        viewModel.onJobSeekerPasswordChanged("Str0ngPass")
        viewModel.onJobSeekerConfirmPasswordChanged("Str0ngPass")
        viewModel.onJobSeekerTermsAcceptedChanged(true)

        viewModel.submitJobSeekerRegistration()

        val args = fakeAuthRepo.lastJobSeekerRegistration
        assertNotNull(args)
        assertEquals("Jane Doe", args?.fullName)
        assertEquals("jane@example.com", args?.email)
    }

    @Test
    fun `company registration with invalid fields does not call the repo`() = runTest {
        viewModel.submitCompanyRegistration()

        assertNull(fakeAuthRepo.lastCompanyRegistration)
        val state = viewModel.companyRegistrationUiState.value
        assertNotNull(state.companyNameError)
        assertNotNull(state.contactPersonNameError)
    }

    @Test
    fun `valid company registration calls registerCompany`() = runTest {
        viewModel.onCompanyNameChanged("Acme Pvt Ltd")
        viewModel.onCompanyContactPersonNameChanged("John Smith")
        viewModel.onCompanyEmailChanged("hr@acme.com")
        viewModel.onCompanyPhoneChanged("9812345678")
        viewModel.onCompanyAddressChanged("Lalitpur")
        viewModel.onCompanyPasswordChanged("Str0ngPass")
        viewModel.onCompanyConfirmPasswordChanged("Str0ngPass")
        viewModel.onCompanyTermsAcceptedChanged(true)

        viewModel.submitCompanyRegistration()

        val args = fakeAuthRepo.lastCompanyRegistration
        assertNotNull(args)
        assertEquals("Acme Pvt Ltd", args?.companyName)
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
        fakeAuthRepo.sendPasswordResetResult = ResultState.Success(Unit)
        viewModel.onPasswordResetEmailChanged("jane@example.com")

        viewModel.submitPasswordReset()

        val state = viewModel.passwordResetUiState.value
        assertTrue(state.isSubmitted)
        assertFalse(state.isLoading)
    }
}
