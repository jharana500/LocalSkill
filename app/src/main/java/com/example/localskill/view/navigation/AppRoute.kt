package com.example.localskill.view.navigation

sealed class AppRoute(val route: String) {
    data object Foundation : AppRoute("foundation")
    data object AuthPlaceholder : AppRoute("auth_placeholder")
}
