package com.example.localskill.view.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.localskill.model.UserRole
import com.example.localskill.view.jobseeker.application.ApplicationDetailsScreen
import com.example.localskill.view.jobseeker.application.ApplicationsScreen
import com.example.localskill.view.jobseeker.application.ApplyJobScreen
import com.example.localskill.view.jobseeker.home.JobSeekerHomeScreen
import com.example.localskill.view.jobseeker.jobs.JobDetailsScreen
import com.example.localskill.view.jobseeker.jobs.JobSearchScreen
import com.example.localskill.view.jobseeker.profile.EditPersonalInfoScreen
import com.example.localskill.view.jobseeker.profile.JobSeekerProfileScreen
import com.example.localskill.view.jobseeker.profile.ManageEducationScreen
import com.example.localskill.view.jobseeker.profile.ManageExperienceScreen
import com.example.localskill.view.jobseeker.profile.ManageResumeScreen
import com.example.localskill.view.jobseeker.profile.ManageSkillsScreen
import com.example.localskill.view.jobseeker.saved.SavedJobsScreen
import com.example.localskill.view.jobseeker.scaffold.JobSeekerScaffold
import com.example.localskill.view.jobseeker.scaffold.navigateToJobSeekerTab
import com.example.localskill.view.jobseeker.settings.JobSeekerSettingsScreen
import com.example.localskill.view.notifications.NotificationScreen
import com.example.localskill.viewmodel.ApplicationViewModel
import com.example.localskill.viewmodel.JobSeekerHomeViewModel
import com.example.localskill.viewmodel.JobSeekerProfileViewModel
import com.example.localskill.viewmodel.JobSeekerSettingsViewModel
import com.example.localskill.viewmodel.JobViewModel
import com.example.localskill.viewmodel.LocalSkillViewModelFactory
import com.example.localskill.viewmodel.NotificationViewModel
import com.example.localskill.viewmodel.SavedJobViewModel
import java.net.URLDecoder

private const val PROFILE_SUB_GRAPH_ROUTE = "job_seeker/profile_graph"

/**
 * Profile, personal info, education, experience, skills, and resume all
 * share one JobSeekerProfileViewModel instance scoped to this sub-graph's
 * back-stack entry, so editing a section and returning to the overview
 * reflects the change without every screen reloading independently.
 */
@Composable
private fun sharedProfileViewModel(
    entry: NavBackStackEntry,
    navController: NavHostController,
    viewModelFactory: LocalSkillViewModelFactory
): JobSeekerProfileViewModel {
    val parentEntry = remember(entry) { navController.getBackStackEntry(PROFILE_SUB_GRAPH_ROUTE) }
    return viewModel(factory = viewModelFactory, viewModelStoreOwner = parentEntry)
}

fun NavGraphBuilder.jobSeekerNavGraph(
    navController: NavHostController,
    viewModelFactory: LocalSkillViewModelFactory,
    activeRole: UserRole,
    onLogout: () -> Unit,
    onRoleRejected: () -> Unit
) {
    navigation(startDestination = JobSeekerRoute.Home.route, route = AppRoute.JobSeekerEntry.route) {

        composable(JobSeekerRoute.Home.route) {
            RoleGuarded(activeRole = activeRole, onRoleRejected = onRoleRejected) {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val viewModel: JobSeekerHomeViewModel = viewModel(factory = viewModelFactory)
                JobSeekerScaffold(
                    currentRoute = backStackEntry?.destination?.route,
                    onNavigate = { navController.navigateToJobSeekerTab(it) }
                ) { padding ->
                    JobSeekerHomeScreen(
                        viewModel = viewModel,
                        onSearchSubmit = { query ->
                            navController.navigateToJobSeekerTab(JobSeekerRoute.Explore.createRoute(query))
                        },
                        onCategoryClick = { navController.navigateToJobSeekerTab(JobSeekerRoute.Explore.BASE_ROUTE) },
                        onSeeAllJobsClick = { navController.navigateToJobSeekerTab(JobSeekerRoute.Explore.BASE_ROUTE) },
                        onJobClick = { jobId -> navController.navigate(JobSeekerRoute.JobDetails.createRoute(jobId)) },
                        onCompleteProfileClick = { navController.navigateToJobSeekerTab(JobSeekerRoute.Profile.route) },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }

        composable(
            route = JobSeekerRoute.Explore.route,
            arguments = listOf(navArgument(JobSeekerRoute.Explore.QUERY_ARG) {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val rawQuery = backStackEntry.arguments?.getString(JobSeekerRoute.Explore.QUERY_ARG).orEmpty()
            val initialQuery = runCatching { URLDecoder.decode(rawQuery, "UTF-8") }.getOrDefault("")
            val currentRoute by navController.currentBackStackEntryAsState()
            val viewModel: JobViewModel = viewModel(factory = viewModelFactory)
            JobSeekerScaffold(
                currentRoute = currentRoute?.destination?.route,
                onNavigate = { navController.navigateToJobSeekerTab(it) }
            ) { padding ->
                JobSearchScreen(
                    viewModel = viewModel,
                    initialQuery = initialQuery,
                    onJobClick = { jobId -> navController.navigate(JobSeekerRoute.JobDetails.createRoute(jobId)) },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(JobSeekerRoute.Applications.route) {
            val currentRoute by navController.currentBackStackEntryAsState()
            val viewModel: ApplicationViewModel = viewModel(factory = viewModelFactory)
            JobSeekerScaffold(
                currentRoute = currentRoute?.destination?.route,
                onNavigate = { navController.navigateToJobSeekerTab(it) }
            ) { padding ->
                ApplicationsScreen(
                    viewModel = viewModel,
                    onApplicationClick = { id -> navController.navigate(JobSeekerRoute.ApplicationDetails.createRoute(id)) },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(JobSeekerRoute.Saved.route) {
            val currentRoute by navController.currentBackStackEntryAsState()
            val viewModel: SavedJobViewModel = viewModel(factory = viewModelFactory)
            JobSeekerScaffold(
                currentRoute = currentRoute?.destination?.route,
                onNavigate = { navController.navigateToJobSeekerTab(it) }
            ) { padding ->
                SavedJobsScreen(
                    viewModel = viewModel,
                    onJobClick = { jobId -> navController.navigate(JobSeekerRoute.JobDetails.createRoute(jobId)) },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(
            route = JobSeekerRoute.JobDetails.route,
            arguments = listOf(navArgument(JobSeekerRoute.JOB_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString(JobSeekerRoute.JOB_ID_ARG).orEmpty()
            if (jobId.isBlank()) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                val viewModel: JobViewModel = viewModel(factory = viewModelFactory)
                JobDetailsScreen(
                    viewModel = viewModel,
                    jobId = jobId,
                    onBack = { navController.popBackStack() },
                    onApplyClick = { navController.navigate(JobSeekerRoute.ApplyJob.createRoute(jobId)) }
                )
            }
        }

        composable(
            route = JobSeekerRoute.ApplyJob.route,
            arguments = listOf(navArgument(JobSeekerRoute.JOB_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString(JobSeekerRoute.JOB_ID_ARG).orEmpty()
            if (jobId.isBlank()) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                val viewModel: ApplicationViewModel = viewModel(factory = viewModelFactory)
                ApplyJobScreen(
                    viewModel = viewModel,
                    jobId = jobId,
                    onBack = { navController.popBackStack() },
                    onManageResumeClick = { navController.navigate(JobSeekerRoute.Resume.route) },
                    onSubmissionSuccess = {
                        navController.navigate(JobSeekerRoute.Applications.route) {
                            popUpTo(JobSeekerRoute.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(
            route = JobSeekerRoute.ApplicationDetails.route,
            arguments = listOf(navArgument(JobSeekerRoute.APPLICATION_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString(JobSeekerRoute.APPLICATION_ID_ARG).orEmpty()
            if (applicationId.isBlank()) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                val viewModel: ApplicationViewModel = viewModel(factory = viewModelFactory)
                ApplicationDetailsScreen(
                    viewModel = viewModel,
                    applicationId = applicationId,
                    onBack = { navController.popBackStack() },
                    onViewJobClick = { jobId -> navController.navigate(JobSeekerRoute.JobDetails.createRoute(jobId)) }
                )
            }
        }

        navigation(startDestination = JobSeekerRoute.Profile.route, route = PROFILE_SUB_GRAPH_ROUTE) {
            composable(JobSeekerRoute.Profile.route) { entry ->
                val currentRoute by navController.currentBackStackEntryAsState()
                val viewModel = sharedProfileViewModel(entry, navController, viewModelFactory)
                JobSeekerScaffold(
                    currentRoute = currentRoute?.destination?.route,
                    onNavigate = { navController.navigateToJobSeekerTab(it) }
                ) { padding ->
                    JobSeekerProfileScreen(
                        viewModel = viewModel,
                        onEditPersonalInfoClick = { navController.navigate(JobSeekerRoute.EditProfile.route) },
                        onEducationClick = { navController.navigate(JobSeekerRoute.Education.route) },
                        onExperienceClick = { navController.navigate(JobSeekerRoute.Experience.route) },
                        onSkillsClick = { navController.navigate(JobSeekerRoute.Skills.route) },
                        onResumeClick = { navController.navigate(JobSeekerRoute.Resume.route) },
                        onSettingsClick = { navController.navigate(JobSeekerRoute.Settings.route) },
                        modifier = Modifier.padding(padding)
                    )
                }
            }

            composable(JobSeekerRoute.EditProfile.route) { entry ->
                EditPersonalInfoScreen(
                    viewModel = sharedProfileViewModel(entry, navController, viewModelFactory),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(JobSeekerRoute.Education.route) { entry ->
                ManageEducationScreen(
                    viewModel = sharedProfileViewModel(entry, navController, viewModelFactory),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(JobSeekerRoute.Experience.route) { entry ->
                ManageExperienceScreen(
                    viewModel = sharedProfileViewModel(entry, navController, viewModelFactory),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(JobSeekerRoute.Skills.route) { entry ->
                ManageSkillsScreen(
                    viewModel = sharedProfileViewModel(entry, navController, viewModelFactory),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(JobSeekerRoute.Resume.route) { entry ->
                ManageResumeScreen(
                    viewModel = sharedProfileViewModel(entry, navController, viewModelFactory),
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(JobSeekerRoute.Settings.route) {
            val viewModel: JobSeekerSettingsViewModel = viewModel(factory = viewModelFactory)
            JobSeekerSettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAccountInfoClick = { navController.navigate(JobSeekerRoute.EditProfile.route) },
                onNotificationsClick = { navController.navigate(JobSeekerRoute.Notifications.route) },
                onLogout = onLogout
            )
        }

        composable(JobSeekerRoute.Notifications.route) {
            val viewModel: NotificationViewModel = viewModel(factory = viewModelFactory)
            NotificationScreen(
                viewModel = viewModel,
                role = UserRole.JOB_SEEKER,
                onBack = { navController.popBackStack() },
                onOpenRoute = { route -> navController.navigate(route) { launchSingleTop = true } }
            )
        }
    }
}

@Composable
private fun RoleGuarded(
    activeRole: UserRole,
    onRoleRejected: () -> Unit,
    content: @Composable () -> Unit
) {
    if (activeRole == UserRole.JOB_SEEKER) {
        content()
    } else {
        LaunchedEffect(activeRole) { onRoleRejected() }
    }
}
