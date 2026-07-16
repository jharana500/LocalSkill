package com.example.localskill.view.company.scaffold

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.example.localskill.view.navigation.CompanyRoute

private data class CompanyBottomBarItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val companyBottomBarItems = listOf(
    CompanyBottomBarItem(CompanyRoute.Dashboard.route, "Dashboard", Icons.Default.Dashboard),
    CompanyBottomBarItem(CompanyRoute.Jobs.route, "Jobs", Icons.Default.Work),
    CompanyBottomBarItem(CompanyRoute.Applicants.route, "Applicants", Icons.Default.People),
    CompanyBottomBarItem(CompanyRoute.Profile.route, "Profile", Icons.Default.Person),
    CompanyBottomBarItem(CompanyRoute.Settings.route, "Settings", Icons.Default.Settings)
)

@Composable
fun CompanyBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        companyBottomBarItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = {
                    Text(text = item.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
