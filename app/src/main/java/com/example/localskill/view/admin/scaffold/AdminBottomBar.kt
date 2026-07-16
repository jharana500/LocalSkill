package com.example.localskill.view.admin.scaffold

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Report
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
import com.example.localskill.view.navigation.AdminRoute

private data class AdminBottomBarItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val adminBottomBarItems = listOf(
    AdminBottomBarItem(AdminRoute.Dashboard.route, "Dashboard", Icons.Default.Dashboard),
    AdminBottomBarItem(AdminRoute.Companies.route, "Companies", Icons.Default.Apartment),
    AdminBottomBarItem(AdminRoute.Jobs.route, "Jobs", Icons.Default.Work),
    AdminBottomBarItem(AdminRoute.Reports.route, "Reports", Icons.Default.Report),
    AdminBottomBarItem(AdminRoute.More.route, "More", Icons.Default.MoreHoriz)
)

@Composable
fun AdminBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        adminBottomBarItems.forEach { item ->
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
