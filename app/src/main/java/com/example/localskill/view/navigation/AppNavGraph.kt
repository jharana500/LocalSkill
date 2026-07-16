package com.example.localskill.view.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.localskill.view.common.AuthPlaceholderScreen
import com.example.localskill.view.common.FoundationScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Foundation.route
    ) {
        composable(AppRoute.Foundation.route) {
            FoundationScreen(
                onContinue = { navController.navigate(AppRoute.AuthPlaceholder.route) }
            )
        }
        composable(AppRoute.AuthPlaceholder.route) {
            AuthPlaceholderScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
