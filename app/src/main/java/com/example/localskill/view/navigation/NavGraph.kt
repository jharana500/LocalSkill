package com.example.localskill.view.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.localskill.model.UserRole
import com.example.localskill.view.auth.ForgotPasswordScreen
import com.example.localskill.view.auth.LoginScreen
import com.example.localskill.view.auth.RegisterScreen
import com.example.localskill.view.auth.RoleSelectionScreen
import com.example.localskill.view.auth.SplashScreen
import com.example.localskill.view.dashboard.EmployerDashboardScreen
import com.example.localskill.view.dashboard.WorkerDashboardScreen
import com.example.localskill.view.employer.ApplicationsScreen as EmployerApplicationsScreen
import com.example.localskill.view.employer.PostJobScreen
import com.example.localskill.view.employer.PostedJobsScreen
import com.example.localskill.view.employer.WorkersScreen
import com.example.localskill.view.notification.NotificationsScreen
import com.example.localskill.view.review.AddReviewScreen
import com.example.localskill.view.worker.AddSkillScreen
import com.example.localskill.view.worker.JobDetailsScreen
import com.example.localskill.view.worker.JobsScreen
import com.example.localskill.view.worker.ProfileScreen
import com.example.localskill.viewmodel.auth.ForgotPasswordViewModel
import com.example.localskill.viewmodel.auth.LoginViewModel
import com.example.localskill.viewmodel.auth.RegisterViewModel
import com.example.localskill.viewmodel.auth.RoleSelectionViewModel
import com.example.localskill.viewmodel.auth.SplashViewModel
import com.example.localskill.viewmodel.dashboard.EmployerDashboardViewModel
import com.example.localskill.viewmodel.dashboard.WorkerDashboardViewModel
import com.example.localskill.viewmodel.employer.EmployerApplicationsViewModel
import com.example.localskill.viewmodel.employer.PostJobViewModel
import com.example.localskill.viewmodel.employer.PostedJobsViewModel
import com.example.localskill.viewmodel.employer.WorkersViewModel
import com.example.localskill.viewmodel.notification.NotificationsViewModel
import com.example.localskill.viewmodel.review.AddReviewViewModel
import com.example.localskill.viewmodel.worker.ApplicationViewModel
import com.example.localskill.viewmodel.worker.JobDetailsViewModel
import com.example.localskill.viewmodel.worker.JobViewModel
import com.example.localskill.viewmodel.worker.ProfileViewModel
import com.example.localskill.viewmodel.worker.SkillViewModel

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgotPassword"
    const val ROLE_SELECTION = "roleSelection"
    const val WORKER_DASHBOARD = "workerDashboard"
    const val EMPLOYER_DASHBOARD = "employerDashboard"
    const val WORKER_JOBS = "workerJobs"
    const val POSTED_JOBS = "postedJobs"
    const val WORKER_PROFILE_SETUP = "workerProfileSetup"
    const val ADD_SKILL = "addSkill"
    const val POST_JOB = "postJob"
    const val JOB_DETAILS = "jobDetails/{jobId}"
    const val APPLICATION_TRACKING = "applicationTracking"
    const val EMPLOYER_APPLICATIONS = "employerApplications"
    const val NEARBY_WORKERS = "nearbyWorkers"
    const val NOTIFICATIONS = "notifications"
    const val ADD_REVIEW = "addReview/{applicationId}/{receiverId}"
    const val PROFILE = "profile"

    fun jobDetails(jobId: String): String = "jobDetails/$jobId"
    fun addReview(applicationId: String, receiverId: String): String = "addReview/$applicationId/$receiverId"
}

@Composable
fun LocalSkillNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            val vm: SplashViewModel = viewModel()
            val state = vm.uiState
            LaunchedEffect(Unit) { vm.checkSession() }
            LaunchedEffect(state) {
                when {
                    state.needsLogin -> navController.replaceSplashWith(Routes.LOGIN)
                    state.needsRole -> navController.replaceSplashWith(Routes.ROLE_SELECTION)
                    state.routeToRole == UserRole.WORKER -> navController.replaceSplashWith(Routes.WORKER_DASHBOARD)
                    state.routeToRole == UserRole.EMPLOYER -> navController.replaceSplashWith(Routes.EMPLOYER_DASHBOARD)
                }
            }
            SplashScreen()
        }
        composable(Routes.LOGIN) {
            val vm: LoginViewModel = viewModel()
            val state = vm.uiState
            LaunchedEffect(state.user) {
                when (state.user?.role) {
                    UserRole.WORKER -> navController.clearAuthAndNavigate(Routes.WORKER_DASHBOARD)
                    UserRole.EMPLOYER -> navController.clearAuthAndNavigate(Routes.EMPLOYER_DASHBOARD)
                    null -> if (state.user != null) navController.clearAuthAndNavigate(Routes.ROLE_SELECTION)
                }
            }
            LoginScreen(
                state = state,
                onEmailChange = vm::onEmailChange,
                onPasswordChange = vm::onPasswordChange,
                onLogin = vm::login,
                onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onRegister = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            val vm: RegisterViewModel = viewModel()
            val state = vm.uiState
            LaunchedEffect(state.registeredUserId) {
                if (state.registeredUserId != null) navController.clearAuthAndNavigate(Routes.ROLE_SELECTION)
            }
            RegisterScreen(
                state = state,
                onFullNameChange = vm::onFullNameChange,
                onEmailChange = vm::onEmailChange,
                onPhoneChange = vm::onPhoneChange,
                onPasswordChange = vm::onPasswordChange,
                onConfirmPasswordChange = vm::onConfirmPasswordChange,
                onRegister = vm::register,
                onLogin = { navController.navigate(Routes.LOGIN) }
            )
        }
        composable(Routes.FORGOT_PASSWORD) {
            val vm: ForgotPasswordViewModel = viewModel()
            ForgotPasswordScreen(
                state = vm.uiState,
                onEmailChange = vm::onEmailChange,
                onSubmit = vm::sendResetLink,
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.ROLE_SELECTION) {
            val vm: RoleSelectionViewModel = viewModel()
            val state = vm.uiState
            LaunchedEffect(state.savedRole) {
                when (state.savedRole) {
                    UserRole.WORKER -> navController.clearAppAndNavigate(Routes.WORKER_DASHBOARD)
                    UserRole.EMPLOYER -> navController.clearAppAndNavigate(Routes.EMPLOYER_DASHBOARD)
                    null -> Unit
                }
            }
            RoleSelectionScreen(state, vm::selectRole, vm::saveRole)
        }
        composable(Routes.WORKER_DASHBOARD) {
            val vm: WorkerDashboardViewModel = viewModel()
            LaunchedEffect(Unit) { vm.load() }
            WorkerDashboardScreen(
                state = vm.uiState,
                onAddSkill = { navController.navigate(Routes.ADD_SKILL) },
                onViewJobs = { navController.navigate(Routes.WORKER_JOBS) },
                onApplications = { navController.navigate(Routes.APPLICATION_TRACKING) },
                onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onJobClick = { jobId -> navController.navigate(Routes.jobDetails(jobId)) },
                onProfile = { navController.navigate(Routes.PROFILE) },
                onLogout = {
                    vm.logout()
                    navController.clearAppAndNavigate(Routes.LOGIN)
                }
            )
        }
        composable(Routes.EMPLOYER_DASHBOARD) {
            val vm: EmployerDashboardViewModel = viewModel()
            LaunchedEffect(Unit) { vm.load() }
            EmployerDashboardScreen(
                state = vm.uiState,
                onPostJob = { navController.navigate(Routes.POST_JOB) },
                onViewPostedJobs = { navController.navigate(Routes.POSTED_JOBS) },
                onApplications = { navController.navigate(Routes.EMPLOYER_APPLICATIONS) },
                onWorkers = { navController.navigate(Routes.NEARBY_WORKERS) },
                onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onLogout = {
                    vm.logout()
                    navController.clearAppAndNavigate(Routes.LOGIN)
                }
            )
        }
        composable(Routes.NOTIFICATIONS) {
            val vm: NotificationsViewModel = viewModel()
            LaunchedEffect(Unit) { vm.load() }
            NotificationsScreen(
                state = vm.uiState,
                onMarkAllRead = vm::markAllAsRead,
                onNotificationClick = vm::markAsRead,
                onDelete = vm::delete
            )
        }
        composable(Routes.NEARBY_WORKERS) {
            val vm: WorkersViewModel = viewModel()
            LaunchedEffect(Unit) { vm.load() }
            WorkersScreen(
                state = vm.uiState,
                onSearchChange = vm::onSearchChange,
                onRadiusSelected = vm::onRadiusSelected,
                distanceText = vm::distanceText
            )
        }
        composable(Routes.EMPLOYER_APPLICATIONS) {
            val vm: EmployerApplicationsViewModel = viewModel()
            LaunchedEffect(Unit) { vm.load() }
            EmployerApplicationsScreen(
                state = vm.uiState,
                onFilterSelected = vm::selectStatusFilter,
                onAccept = vm::accept,
                onReject = vm::reject,
                onReview = { application -> navController.navigate(Routes.addReview(application.id, application.workerId)) }
            )
        }
        composable(Routes.WORKER_JOBS) {
            val vm: JobViewModel = viewModel()
            LaunchedEffect(Unit) { vm.loadOpenJobs() }
            JobsScreen(
                state = vm.uiState,
                onSearchChange = vm::onSearchChange,
                onJobTypeSelected = vm::onJobTypeSelected,
                onRadiusSelected = vm::onRadiusSelected,
                distanceText = vm::distanceText,
                onJobClick = { jobId -> navController.navigate(Routes.jobDetails(jobId)) }
            )
        }
        composable(Routes.POSTED_JOBS) {
            val vm: PostedJobsViewModel = viewModel()
            LaunchedEffect(Unit) { vm.load() }
            PostedJobsScreen(vm.uiState)
        }
        composable(Routes.ADD_SKILL) {
            val vm: SkillViewModel = viewModel()
            AddSkillScreen(vm.uiState, vm::update, vm::saveSkill)
        }
        composable(Routes.POST_JOB) {
            val vm: PostJobViewModel = viewModel()
            PostJobScreen(vm.uiState, vm::update, vm::postJob)
        }
        composable(Routes.PROFILE) {
            val vm: ProfileViewModel = viewModel()
            LaunchedEffect(Unit) { vm.load() }
            ProfileScreen(
                state = vm.uiState,
                onUpdate = vm::update,
                onSave = vm::saveProfile,
                onAddSkill = { navController.navigate(Routes.ADD_SKILL) },
                onLogout = {
                    vm.logout()
                    navController.clearAppAndNavigate(Routes.LOGIN)
                }
            )
        }
        composable(Routes.ONBOARDING) { LoginScreen(viewModel<LoginViewModel>().uiState, {}, {}, {}, {}, {}) }
        composable(Routes.WORKER_PROFILE_SETUP) {
            val vm: ProfileViewModel = viewModel()
            LaunchedEffect(Unit) { vm.load() }
            ProfileScreen(
                state = vm.uiState,
                onUpdate = vm::update,
                onSave = vm::saveProfile,
                onAddSkill = { navController.navigate(Routes.ADD_SKILL) },
                onLogout = {
                    vm.logout()
                    navController.clearAppAndNavigate(Routes.LOGIN)
                }
            )
        }
        composable(
            route = Routes.JOB_DETAILS,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { entry ->
            val vm: JobDetailsViewModel = viewModel()
            val jobId = entry.arguments?.getString("jobId").orEmpty()
            LaunchedEffect(jobId) { vm.load(jobId) }
            JobDetailsScreen(
                state = vm.uiState,
                onMessageChange = vm::onApplicationMessageChange,
                onApplyClick = vm::applyToJob
            )
        }
        composable(Routes.APPLICATION_TRACKING) {
            val vm: ApplicationViewModel = viewModel()
            LaunchedEffect(Unit) { vm.load() }
            com.example.localskill.view.worker.ApplicationsScreen(
                state = vm.uiState,
                onReview = { application -> navController.navigate(Routes.addReview(application.id, application.employerId)) }
            )
        }
        composable(
            route = Routes.ADD_REVIEW,
            arguments = listOf(
                navArgument("applicationId") { type = NavType.StringType },
                navArgument("receiverId") { type = NavType.StringType }
            )
        ) { entry ->
            val vm: AddReviewViewModel = viewModel()
            val applicationId = entry.arguments?.getString("applicationId").orEmpty()
            val receiverId = entry.arguments?.getString("receiverId").orEmpty()
            LaunchedEffect(applicationId, receiverId) { vm.load(applicationId, receiverId) }
            AddReviewScreen(
                state = vm.uiState,
                onRatingSelected = vm::onRatingSelected,
                onCommentChange = vm::onCommentChange,
                onSubmit = vm::submit
            )
        }
    }
}

private fun NavHostController.replaceSplashWith(route: String) {
    navigate(route) {
        popUpTo(Routes.SPLASH) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavHostController.clearAuthAndNavigate(route: String) {
    navigate(route) {
        popUpTo(Routes.LOGIN) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavHostController.clearAppAndNavigate(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
