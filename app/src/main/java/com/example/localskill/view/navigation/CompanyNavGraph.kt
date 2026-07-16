package com.example.localskill.view.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.localskill.model.UserRole
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.common.states.EmptyState
import com.example.localskill.view.company.applicants.ApplicantDetailsScreen
import com.example.localskill.view.company.applicants.ApplicantsScreen
import com.example.localskill.view.company.dashboard.CompanyDashboardScreen
import com.example.localskill.view.company.jobs.CompanyJobDetailsScreen
import com.example.localskill.view.company.jobs.CompanyJobFormScreen
import com.example.localskill.view.company.jobs.CompanyJobsScreen
import com.example.localskill.view.company.profile.CompanyProfileScreen
import com.example.localskill.view.company.profile.EditCompanyProfileScreen
import com.example.localskill.view.company.scaffold.CompanyScaffold
import com.example.localskill.view.company.scaffold.navigateToCompanyTab
import com.example.localskill.view.company.settings.CompanySettingsScreen
import com.example.localskill.view.company.verification.CompanyVerificationScreen
import com.example.localskill.viewmodel.ApplicantViewModel
import com.example.localskill.viewmodel.CompanyDashboardViewModel
import com.example.localskill.viewmodel.CompanyJobViewModel
import com.example.localskill.viewmodel.CompanyProfileViewModel
import com.example.localskill.viewmodel.CompanySettingsViewModel
import com.example.localskill.viewmodel.CompanyVerificationViewModel
import com.example.localskill.viewmodel.LocalSkillViewModelFactory

fun NavGraphBuilder.companyNavGraph(
    navController: NavHostController,
    viewModelFactory: LocalSkillViewModelFactory,
    activeRole: UserRole,
    companyRestrictedMode: Boolean,
    onLogout: () -> Unit,
    onRoleRejected: () -> Unit
) {
    navigation(startDestination = CompanyRoute.Dashboard.route, route = AppRoute.CompanyEntry.route) {

        composable(CompanyRoute.Dashboard.route) {
            RoleGuardedCompany(activeRole = activeRole, onRoleRejected = onRoleRejected) {
                val currentRoute by navController.currentBackStackEntryAsState()
                val viewModel: CompanyDashboardViewModel = viewModel(factory = viewModelFactory)
                CompanyScaffold(
                    currentRoute = currentRoute?.destination?.route,
                    onNavigate = { navController.navigateToCompanyTab(it) }
                ) { padding ->
                    CompanyDashboardScreen(
                        viewModel = viewModel,
                        onPostJobClick = { navController.navigate(CompanyRoute.JobForm.createRoute()) },
                        onReviewApplicantsClick = { navController.navigateToCompanyTab(CompanyRoute.Applicants.route) },
                        onVerificationClick = { navController.navigate(CompanyRoute.Verification.route) },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }

        composable(CompanyRoute.Jobs.route) {
            val currentRoute by navController.currentBackStackEntryAsState()
            CompanyScaffold(
                currentRoute = currentRoute?.destination?.route,
                onNavigate = { navController.navigateToCompanyTab(it) }
            ) { padding ->
                if (companyRestrictedMode) {
                    RestrictedFeatureNotice(
                        modifier = Modifier.padding(padding),
                        onVerificationClick = { navController.navigate(CompanyRoute.Verification.route) }
                    )
                } else {
                    val viewModel: CompanyJobViewModel = viewModel(factory = viewModelFactory)
                    CompanyJobsScreen(
                        viewModel = viewModel,
                        onJobClick = { jobId -> navController.navigate(CompanyRoute.JobDetails.createRoute(jobId)) },
                        onPostJobClick = { navController.navigate(CompanyRoute.JobForm.createRoute()) },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }

        composable(CompanyRoute.Applicants.route) {
            val currentRoute by navController.currentBackStackEntryAsState()
            CompanyScaffold(
                currentRoute = currentRoute?.destination?.route,
                onNavigate = { navController.navigateToCompanyTab(it) }
            ) { padding ->
                if (companyRestrictedMode) {
                    RestrictedFeatureNotice(
                        modifier = Modifier.padding(padding),
                        onVerificationClick = { navController.navigate(CompanyRoute.Verification.route) }
                    )
                } else {
                    val viewModel: ApplicantViewModel = viewModel(factory = viewModelFactory)
                    ApplicantsScreen(
                        viewModel = viewModel,
                        onApplicantClick = { id -> navController.navigate(CompanyRoute.ApplicantDetails.createRoute(id)) },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }

        composable(CompanyRoute.Profile.route) {
            val currentRoute by navController.currentBackStackEntryAsState()
            val viewModel: CompanyProfileViewModel = viewModel(factory = viewModelFactory)
            CompanyScaffold(
                currentRoute = currentRoute?.destination?.route,
                onNavigate = { navController.navigateToCompanyTab(it) }
            ) { padding ->
                CompanyProfileScreen(
                    viewModel = viewModel,
                    onEditProfileClick = { navController.navigate(CompanyRoute.EditProfile.route) },
                    onVerificationClick = { navController.navigate(CompanyRoute.Verification.route) },
                    onSettingsClick = { navController.navigate(CompanyRoute.Settings.route) },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(CompanyRoute.Settings.route) {
            val viewModel: CompanySettingsViewModel = viewModel(factory = viewModelFactory)
            CompanySettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onVerificationClick = { navController.navigate(CompanyRoute.Verification.route) },
                onLogout = onLogout
            )
        }

        composable(CompanyRoute.Verification.route) {
            val viewModel: CompanyVerificationViewModel = viewModel(factory = viewModelFactory)
            CompanyVerificationScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate(CompanyRoute.EditProfile.route) }
            )
        }

        composable(CompanyRoute.EditProfile.route) {
            val viewModel: CompanyProfileViewModel = viewModel(factory = viewModelFactory)
            EditCompanyProfileScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(
            route = CompanyRoute.JobForm.route,
            arguments = listOf(navArgument(CompanyRoute.JOB_ID_ARG) {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString(CompanyRoute.JOB_ID_ARG).orEmpty()
            val viewModel: CompanyJobViewModel = viewModel(factory = viewModelFactory)
            CompanyJobFormScreen(
                viewModel = viewModel,
                jobId = jobId.ifBlank { null },
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onPublished = { navController.popBackStack() }
            )
        }

        composable(
            route = CompanyRoute.JobDetails.route,
            arguments = listOf(navArgument(CompanyRoute.JOB_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString(CompanyRoute.JOB_ID_ARG).orEmpty()
            val viewModel: CompanyJobViewModel = viewModel(factory = viewModelFactory)
            CompanyJobDetailsScreen(
                viewModel = viewModel,
                jobId = jobId,
                onBack = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate(CompanyRoute.JobForm.createRoute(id)) },
                onViewApplicantsClick = { id -> navController.navigate(CompanyRoute.JobApplicants.createRoute(id)) }
            )
        }

        composable(
            route = CompanyRoute.JobApplicants.route,
            arguments = listOf(navArgument(CompanyRoute.JOB_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString(CompanyRoute.JOB_ID_ARG).orEmpty()
            val viewModel: ApplicantViewModel = viewModel(factory = viewModelFactory)
            LaunchedEffect(jobId) { viewModel.loadApplicants(jobId) }
            Scaffold(topBar = { LocalSkillTopAppBar(title = "Job applicants", onBack = { navController.popBackStack() }) }) { padding ->
                ApplicantsScreen(
                    viewModel = viewModel,
                    onApplicantClick = { id -> navController.navigate(CompanyRoute.ApplicantDetails.createRoute(id)) },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(
            route = CompanyRoute.ApplicantDetails.route,
            arguments = listOf(navArgument(CompanyRoute.APPLICATION_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString(CompanyRoute.APPLICATION_ID_ARG).orEmpty()
            val viewModel: ApplicantViewModel = viewModel(factory = viewModelFactory)
            ApplicantDetailsScreen(
                viewModel = viewModel,
                applicationId = applicationId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun RestrictedFeatureNotice(
    onVerificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        EmptyState(
            title = "Verification required",
            description = "Complete company verification to unlock job posting and applicant management.",
            modifier = Modifier.weight(1f)
        )
        LocalSkillPrimaryButton(
            text = "Go to verification",
            onClick = onVerificationClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}

@Composable
private fun RoleGuardedCompany(
    activeRole: UserRole,
    onRoleRejected: () -> Unit,
    content: @Composable () -> Unit
) {
    if (activeRole == UserRole.COMPANY) {
        content()
    } else {
        LaunchedEffect(activeRole) { onRoleRejected() }
    }
}
