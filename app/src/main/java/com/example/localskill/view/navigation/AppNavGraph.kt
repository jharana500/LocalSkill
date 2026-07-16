package com.example.localskill.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.localskill.view.auth.login.LoginScreen
import com.example.localskill.view.auth.passwordreset.ForgotPasswordScreen
import com.example.localskill.view.auth.registration.CompanyRegistrationScreen
import com.example.localskill.view.auth.registration.JobSeekerRegistrationScreen
import com.example.localskill.view.auth.registration.RoleSelectionScreen
import com.example.localskill.view.auth.verification.EmailVerificationScreen
import com.example.localskill.view.entry.AccountStatusScreen
import com.example.localskill.view.entry.AdminEntryScreen
import com.example.localskill.view.entry.CompanyEntryScreen
import com.example.localskill.view.entry.JobSeekerEntryScreen
import com.example.localskill.view.onboarding.OnboardingScreen
import com.example.localskill.view.splash.SplashScreen
import com.example.localskill.viewmodel.AppSessionViewModel
import com.example.localskill.viewmodel.AuthViewModel
import com.example.localskill.viewmodel.LocalSkillViewModelFactory
import com.example.localskill.viewmodel.OnboardingViewModel
import com.example.localskill.viewmodel.SessionDestination

@Composable
fun AppNavGraph(
    sessionViewModel: AppSessionViewModel,
    viewModelFactory: LocalSkillViewModelFactory,
    navController: NavHostController = rememberNavController()
) {
    val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()

    // Every session-driven transition (initial launch, login, registration,
    // email verification, logout) is decided here from AppSessionViewModel's
    // trusted state and clears the entire back stack, so onboarding/auth
    // screens can never be reached again via the back button afterward.
    LaunchedEffect(sessionUiState.destination, sessionUiState.isLoading) {
        if (sessionUiState.isLoading) return@LaunchedEffect

        val targetRoute = when (sessionUiState.destination) {
            SessionDestination.UNKNOWN -> return@LaunchedEffect
            SessionDestination.ONBOARDING -> AppRoute.Onboarding.route
            SessionDestination.ROLE_SELECTION -> AppRoute.RoleSelection.route
            SessionDestination.EMAIL_VERIFICATION -> AppRoute.EmailVerification.route
            SessionDestination.ACCOUNT_STATUS -> AppRoute.AccountStatus.route
            SessionDestination.JOB_SEEKER_ENTRY -> AppRoute.JobSeekerEntry.route
            SessionDestination.COMPANY_ENTRY -> AppRoute.CompanyEntry.route
            SessionDestination.ADMIN_ENTRY -> AppRoute.AdminEntry.route
        }

        if (navController.currentDestination?.route != targetRoute) {
            navController.navigate(targetRoute) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route
    ) {
        composable(AppRoute.Splash.route) {
            SplashScreen()
        }

        composable(AppRoute.Onboarding.route) {
            val onboardingViewModel: OnboardingViewModel = viewModel(factory = viewModelFactory)
            LaunchedEffect(Unit) {
                onboardingViewModel.completedEvent.collect {
                    sessionViewModel.evaluateSession()
                }
            }
            OnboardingScreen(onFinished = onboardingViewModel::completeOnboarding)
        }

        composable(AppRoute.RoleSelection.route) {
            RoleSelectionScreen(
                onJobSeekerSelected = { navController.navigate(AppRoute.JobSeekerRegistration.route) },
                onCompanySelected = { navController.navigate(AppRoute.CompanyRegistration.route) },
                onLoginClick = { navController.navigate(AppRoute.Login.route) }
            )
        }

        composable(AppRoute.Login.route) {
            val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { sessionViewModel.evaluateSession() },
                onForgotPasswordClick = { navController.navigate(AppRoute.ForgotPassword.route) },
                onCreateAccountClick = { navController.popBackStack() }
            )
        }

        composable(AppRoute.JobSeekerRegistration.route) {
            val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
            JobSeekerRegistrationScreen(
                viewModel = authViewModel,
                onRegistrationSuccess = { sessionViewModel.evaluateSession() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.CompanyRegistration.route) {
            val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
            CompanyRegistrationScreen(
                viewModel = authViewModel,
                onRegistrationSuccess = { sessionViewModel.evaluateSession() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.ForgotPassword.route) {
            val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.EmailVerification.route) {
            val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
            EmailVerificationScreen(
                viewModel = authViewModel,
                onVerified = { sessionViewModel.evaluateSession() },
                onSignOut = { sessionViewModel.logout() }
            )
        }

        composable(AppRoute.AccountStatus.route) {
            AccountStatusScreen(
                accountStatus = sessionUiState.accountStatus,
                onLogout = { sessionViewModel.logout() }
            )
        }

        composable(AppRoute.JobSeekerEntry.route) {
            JobSeekerEntryScreen(onLogout = { sessionViewModel.logout() })
        }

        composable(AppRoute.CompanyEntry.route) {
            CompanyEntryScreen(onLogout = { sessionViewModel.logout() })
        }

        composable(AppRoute.AdminEntry.route) {
            AdminEntryScreen(onLogout = { sessionViewModel.logout() })
        }
    }
}
