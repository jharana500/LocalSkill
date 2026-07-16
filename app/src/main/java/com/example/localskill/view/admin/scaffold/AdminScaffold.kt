package com.example.localskill.view.admin.scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.localskill.view.navigation.AdminRoute

/**
 * Wraps one of the five main bottom-navigation destinations. Detail screens
 * use a plain Scaffold with a back-button top bar instead, so the bottom
 * bar never appears on them.
 */
@Composable
fun AdminScaffold(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = { AdminBottomBar(currentRoute = currentRoute, onNavigate = onNavigate) }
    ) { innerPadding ->
        content(innerPadding)
    }
}

/** Standard single-top bottom-nav navigation: no duplicate destinations, state is preserved per tab. */
fun NavHostController.navigateToAdminTab(route: String) {
    navigate(route) {
        popUpTo(AdminRoute.Dashboard.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
