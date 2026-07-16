package com.example.localskill.view.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.localskill.model.UserRole
import com.example.localskill.view.admin.activity.AdminActivityScreen
import com.example.localskill.view.admin.categories.AdminCategoriesScreen
import com.example.localskill.view.admin.companies.AdminCompaniesScreen
import com.example.localskill.view.admin.companies.AdminCompanyDetailsScreen
import com.example.localskill.view.admin.dashboard.AdminDashboardScreen
import com.example.localskill.view.admin.jobs.AdminJobsScreen
import com.example.localskill.view.admin.more.AdminMoreScreen
import com.example.localskill.view.admin.reports.AdminReportDetailsScreen
import com.example.localskill.view.admin.reports.AdminReportsScreen
import com.example.localskill.view.admin.scaffold.AdminScaffold
import com.example.localskill.view.admin.scaffold.navigateToAdminTab
import com.example.localskill.view.admin.settings.AdminSettingsScreen
import com.example.localskill.view.admin.users.AdminUsersScreen
import com.example.localskill.view.notifications.NotificationScreen
import com.example.localskill.viewmodel.AdminCategoryViewModel
import com.example.localskill.viewmodel.AdminCompanyViewModel
import com.example.localskill.viewmodel.AdminDashboardViewModel
import com.example.localskill.viewmodel.AdminJobViewModel
import com.example.localskill.viewmodel.AdminReportViewModel
import com.example.localskill.viewmodel.AdminSettingsViewModel
import com.example.localskill.viewmodel.AdminUserViewModel
import com.example.localskill.viewmodel.LocalSkillViewModelFactory
import com.example.localskill.viewmodel.NotificationViewModel

fun NavGraphBuilder.adminNavGraph(
    navController: NavHostController,
    viewModelFactory: LocalSkillViewModelFactory,
    activeRole: UserRole,
    onLogout: () -> Unit,
    onRoleRejected: () -> Unit
) {
    navigation(startDestination = AdminRoute.Dashboard.route, route = AppRoute.AdminEntry.route) {

        composable(AdminRoute.Dashboard.route) {
            RoleGuardedAdmin(activeRole = activeRole, onRoleRejected = onRoleRejected) {
                val currentRoute by navController.currentBackStackEntryAsState()
                val viewModel: AdminDashboardViewModel = viewModel(factory = viewModelFactory)
                AdminScaffold(
                    currentRoute = currentRoute?.destination?.route,
                    onNavigate = { navController.navigateToAdminTab(it) }
                ) { padding ->
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        onPendingCompanyClick = { id -> navController.navigate(AdminRoute.CompanyDetails.createRoute(id)) },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }

        composable(AdminRoute.Companies.route) {
            val currentRoute by navController.currentBackStackEntryAsState()
            val viewModel: AdminCompanyViewModel = viewModel(factory = viewModelFactory)
            AdminScaffold(
                currentRoute = currentRoute?.destination?.route,
                onNavigate = { navController.navigateToAdminTab(it) }
            ) { padding ->
                AdminCompaniesScreen(
                    viewModel = viewModel,
                    onCompanyClick = { id -> navController.navigate(AdminRoute.CompanyDetails.createRoute(id)) },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(AdminRoute.Jobs.route) {
            val currentRoute by navController.currentBackStackEntryAsState()
            val viewModel: AdminJobViewModel = viewModel(factory = viewModelFactory)
            AdminScaffold(
                currentRoute = currentRoute?.destination?.route,
                onNavigate = { navController.navigateToAdminTab(it) }
            ) { padding ->
                AdminJobsScreen(viewModel = viewModel, modifier = Modifier.padding(padding))
            }
        }

        composable(AdminRoute.Reports.route) {
            val currentRoute by navController.currentBackStackEntryAsState()
            val viewModel: AdminReportViewModel = viewModel(factory = viewModelFactory)
            AdminScaffold(
                currentRoute = currentRoute?.destination?.route,
                onNavigate = { navController.navigateToAdminTab(it) }
            ) { padding ->
                AdminReportsScreen(
                    viewModel = viewModel,
                    onReportClick = { id -> navController.navigate(AdminRoute.ReportDetails.createRoute(id)) },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(AdminRoute.More.route) {
            val currentRoute by navController.currentBackStackEntryAsState()
            AdminScaffold(
                currentRoute = currentRoute?.destination?.route,
                onNavigate = { navController.navigateToAdminTab(it) }
            ) { padding ->
                AdminMoreScreen(
                    onUsersClick = { navController.navigate(AdminRoute.Users.route) },
                    onCategoriesClick = { navController.navigate(AdminRoute.Categories.route) },
                    onActivityClick = { navController.navigate(AdminRoute.Activity.route) },
                    onSettingsClick = { navController.navigate(AdminRoute.Settings.route) },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(AdminRoute.Users.route) {
            val viewModel: AdminUserViewModel = viewModel(factory = viewModelFactory)
            AdminUsersScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(AdminRoute.Categories.route) {
            val viewModel: AdminCategoryViewModel = viewModel(factory = viewModelFactory)
            AdminCategoriesScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(AdminRoute.Activity.route) {
            val viewModel: AdminDashboardViewModel = viewModel(factory = viewModelFactory)
            AdminActivityScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(AdminRoute.Settings.route) {
            val viewModel: AdminSettingsViewModel = viewModel(factory = viewModelFactory)
            AdminSettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNotificationsClick = { navController.navigate(AdminRoute.Notifications.route) },
                onLogout = onLogout
            )
        }

        composable(AdminRoute.Notifications.route) {
            val viewModel: NotificationViewModel = viewModel(factory = viewModelFactory)
            NotificationScreen(
                viewModel = viewModel,
                role = UserRole.ADMIN,
                onBack = { navController.popBackStack() },
                onOpenRoute = { route -> navController.navigate(route) { launchSingleTop = true } }
            )
        }

        composable(
            route = AdminRoute.CompanyDetails.route,
            arguments = listOf(navArgument(AdminRoute.COMPANY_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val companyId = backStackEntry.arguments?.getString(AdminRoute.COMPANY_ID_ARG).orEmpty()
            val viewModel: AdminCompanyViewModel = viewModel(factory = viewModelFactory)
            AdminCompanyDetailsScreen(
                viewModel = viewModel,
                companyId = companyId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AdminRoute.ReportDetails.route,
            arguments = listOf(navArgument(AdminRoute.REPORT_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString(AdminRoute.REPORT_ID_ARG).orEmpty()
            val viewModel: AdminReportViewModel = viewModel(factory = viewModelFactory)
            AdminReportDetailsScreen(
                viewModel = viewModel,
                reportId = reportId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun RoleGuardedAdmin(
    activeRole: UserRole,
    onRoleRejected: () -> Unit,
    content: @Composable () -> Unit
) {
    if (activeRole == UserRole.ADMIN) {
        content()
    } else {
        LaunchedEffect(activeRole) { onRoleRejected() }
    }
}
